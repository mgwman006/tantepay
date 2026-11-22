package pay.tante.tz.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;


import pay.tante.tz.clients.RentManagerClient;
import pay.tante.tz.enums.TanteService;
import pay.tante.tz.enums.VirtualAccountEntryType;
import pay.tante.tz.exceptions.ResourceNotFoundException;
import pay.tante.tz.models.dtos.requests.CreateGenericPaymentIntentDTO;
import pay.tante.tz.models.dtos.requests.VirtualAccountTransactionDTO;
import pay.tante.tz.models.dtos.responses.PaymentIntentDetailsDTO;
import pay.tante.tz.models.entities.PaymentIntent;
import pay.tante.tz.repositories.PaymentIntentRepository;



@Service
public class PaymentIntentService {
  private static final int DEFAULT_EXPIRY_MINUTES = 15;

  private final PaymentIntentRepository paymentIntentRepository;
  private final VirtualAccountService virtualAccountService;
  private final RentManagerClient rentManagerClient;
  private final String tantePayUrl;

  public PaymentIntentService(
    @Value("${tante.pay.url}")
    String tantePayUrl,
    PaymentIntentRepository paymentIntentRepository,
    VirtualAccountService virtualAccountService,
    RentManagerClient rentManagerClient
  )
  {
    this.tantePayUrl = tantePayUrl;
    this.paymentIntentRepository = paymentIntentRepository;
    this.virtualAccountService = virtualAccountService;
    this.rentManagerClient = rentManagerClient;
  }

  @Transactional
  public PaymentIntentDetailsDTO createPaymentIntent(CreateGenericPaymentIntentDTO request)
  {
    PaymentIntent pendingIntent = paymentIntentRepository
      .findByServiceReferenceNumber(request.serviceReferenceNumber())
      .orElse(null);

    if (pendingIntent != null)
    {
      return toDetails(pendingIntent);
    }

    PaymentIntent intent = new PaymentIntent();
    intent.setUserAccountId(request.userAccountId());
    intent.setService(request.service());
    intent.setServiceReferenceNumber(request.serviceReferenceNumber());
    intent.setAmount(request.amount());
    intent.setCurrency(request.currency());
    intent.setStatusCallbackUrl(request.statusCallbackUrl());
    intent.setPaymentLink(generatePaymentLink(request.serviceReferenceNumber()));
    intent.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
    intent.setExpiresAt(intent.getCreatedAt().plusMinutes(DEFAULT_EXPIRY_MINUTES));
    intent = paymentIntentRepository.save(intent);
    return toDetails(intent);
  }

  @Transactional
  public PaymentIntentDetailsDTO payWithVirtualAccount(String serviceReferenceNumber) {
    PaymentIntent intent = findIntentByServiceReferenceNumber(serviceReferenceNumber);

    virtualAccountService.debit(intent.getUserAccountId(), new VirtualAccountTransactionDTO(
      intent.getAmount(),
      VirtualAccountEntryType.VIEWING_FEE_PAYMENT,
      intent.getServiceReferenceNumber() + "-VA",
      "Virtual account payment for " + intent.getService() + " with reference " + intent.getServiceReferenceNumber()
    ));

    intent.setPaidAt(LocalDateTime.now(ZoneId.of("UTC")));
    intent = paymentIntentRepository.save(intent);

    notifyCallingService(intent, "PAID", null);
    return toDetails(intent);
  }

  private void notifyCallingService(PaymentIntent intent, String paymentStatus, String reason)
  {
    if (StringUtils.hasText(intent.getStatusCallbackUrl()))
    {
      try
      {
        RestClient.create()
          .post()
          .uri(intent.getStatusCallbackUrl())
          .body(Map.of(
            "reference", intent.getServiceReferenceNumber(),
            "paymentStatus", paymentStatus,
            "reason", reason != null ? reason : ""
          ))
          .retrieve()
          .toBodilessEntity();
      } catch (Exception ex) {
        System.err.println("Failed to notify calling service: " + ex.getMessage());
      }
    }

    if (intent.getService() == TanteService.VIEWING_BOOKING )
    {
      rentManagerClient.updateViewingPaymentStatus(intent.getStatusCallbackUrl(), paymentStatus, reason);
    }
  }

  public PaymentIntentDetailsDTO getIntent(String serviceReferenceNumber) {
    PaymentIntent intent = findIntentByServiceReferenceNumber(serviceReferenceNumber);
    return toDetails(intent);
  }

  public PaymentIntent findIntentByServiceReferenceNumber(String serviceReferenceNumber)
  {
    return paymentIntentRepository.findByServiceReferenceNumber(serviceReferenceNumber)
      .orElseThrow(() -> new ResourceNotFoundException("Payment intent " + serviceReferenceNumber + " not found."));
  }

  private PaymentIntentDetailsDTO toDetails(PaymentIntent intent)
  {
    return new PaymentIntentDetailsDTO(
      intent.getId(),
      intent.getUserAccountId(),
      intent.getService(),
      intent.getServiceReferenceNumber(),
      intent.getAmount(),
      intent.getCurrency(),
      intent.getStatusCallbackUrl(),
      intent.getPaymentLink(),
      intent.getStatus(),
      intent.getCreatedAt(),
      intent.getExpiresAt(),
      intent.getPaidAt(),
      intent.getRefundedAt()
    );
  }

  private String generatePaymentLink(String serviceReferenceNumber)
  {
    return tantePayUrl + "/"+  serviceReferenceNumber+"/checkout";
  }
}
