package pay.tante.tz.services;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import java.time.LocalDateTime;
import java.time.ZoneId;

import pay.tante.tz.enums.PaymentIntentStatus;
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

  /**
   * Constants
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PaymentIntentService.class);
  private static final int DEFAULT_EXPIRY_MINUTES = 15;

  /**
   * Variables
   */
  private final PaymentIntentRepository paymentIntentRepository;
  private final VirtualAccountService virtualAccountService;
  private final String tantePayUrl;

  public PaymentIntentService(
    @Value("${tante.pay.url}")
    String tantePayUrl,
    PaymentIntentRepository paymentIntentRepository,
    VirtualAccountService virtualAccountService
  )
  {
    this.tantePayUrl = tantePayUrl;
    this.paymentIntentRepository = paymentIntentRepository;
    this.virtualAccountService = virtualAccountService;
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
    intent.setCallbackUrl(request.callbackUrl());
    intent.setPaymentLink(generatePaymentLink(request.serviceReferenceNumber()));
    intent.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
    intent.setExpiresAt(intent.getCreatedAt().plusMinutes(DEFAULT_EXPIRY_MINUTES));
    intent = paymentIntentRepository.save(intent);
    return toDetails(intent);
  }

  @Transactional
  public PaymentIntentDetailsDTO payWithVirtualAccount(String serviceReferenceNumber) {
    PaymentIntent intent = findIntentByServiceReferenceNumber(serviceReferenceNumber);

    VirtualAccountTransactionDTO transaction = new VirtualAccountTransactionDTO(
      intent.getAmount(),
      intent.getService() == TanteService.VIEWING_BOOKING ? VirtualAccountEntryType.VIEWING_FEE_PAYMENT : VirtualAccountEntryType.GENERIC_PAYMENT,
      intent.getServiceReferenceNumber(),
      "Virtual account payment for " + intent.getService() + " with reference " + intent.getServiceReferenceNumber()
    );

    virtualAccountService.debit(intent.getUserAccountId(), transaction);

    intent.setPaidAt(LocalDateTime.now(ZoneId.of("UTC")));
    intent.setStatus(PaymentIntentStatus.PAID);
    intent = paymentIntentRepository.save(intent);

    notifyCallingService(intent);
    return toDetails(intent);
  }

  private void notifyCallingService(PaymentIntent intent)
  {
    if (StringUtils.hasText(intent.getCallbackUrl()))
    {
      PaymentIntentDetailsDTO details = toDetails(intent);
      try
      {
        RestClient.create()
          .post()
          .uri(intent.getCallbackUrl())
          .body(details)
          .retrieve()
          .toBodilessEntity();
      }
      catch (Exception ex)
      {
        LOGGER.error("Failed to notify calling service", ex);
      }
    }
    else
    {
      LOGGER.warn("No callback URL provided for payment intent {}", intent.getServiceReferenceNumber());
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
      intent.getCallbackUrl(),
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
