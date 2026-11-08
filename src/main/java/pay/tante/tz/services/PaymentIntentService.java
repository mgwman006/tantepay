package pay.tante.tz.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pay.tante.tz.clients.RentManagerClient;
import pay.tante.tz.enums.PaymentIntentStatus;
import pay.tante.tz.enums.PaymentPurpose;
import pay.tante.tz.enums.VirtualAccountEntryType;
import pay.tante.tz.exceptions.ResourceNotFoundException;
import pay.tante.tz.exceptions.TantePayException;
import pay.tante.tz.models.dtos.requests.CreatePaymentIntentDTO;
import pay.tante.tz.models.dtos.requests.PaymentCallbackDTO;
import pay.tante.tz.models.dtos.requests.VirtualAccountTransactionDTO;
import pay.tante.tz.models.dtos.responses.PaymentIntentDetailsDTO;
import pay.tante.tz.models.dtos.responses.ViewingBookingPaymentDTO;
import pay.tante.tz.models.entities.PaymentIntent;
import pay.tante.tz.providers.PaymentProviderAdapter;
import pay.tante.tz.repositories.PaymentIntentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentIntentService {
  private static final int DEFAULT_EXPIRY_MINUTES = 15;

  private final PaymentIntentRepository paymentIntentRepository;
  private final VirtualAccountService virtualAccountService;
  private final RentManagerClient rentManagerClient;
  private final List<PaymentProviderAdapter> providerAdapters;

  @Transactional
  public PaymentIntentDetailsDTO createViewingPayment(CreatePaymentIntentDTO request) {
    PaymentIntent pendingIntent = paymentIntentRepository
      .findFirstByBookingReferenceAndStatusOrderByIdDesc(request.bookingReference(), PaymentIntentStatus.PENDING)
      .orElse(null);

    if (pendingIntent != null) {
      return toDetails(pendingIntent);
    }

    if (paymentIntentRepository
      .findFirstByBookingReferenceAndStatusOrderByIdDesc(request.bookingReference(), PaymentIntentStatus.SUCCESSFUL)
      .isPresent()) {
      throw new TantePayException("Viewing booking " + request.bookingReference() + " has already been paid.");
    }

    ViewingBookingPaymentDTO booking = rentManagerClient.fetchViewingBooking(request.bookingReference());

    if (booking.serviceFee() == null || booking.serviceFee().compareTo(BigDecimal.ZERO) <= 0) {
      throw new TantePayException("Viewing booking " + request.bookingReference() + " has no payable viewing fee.");
    }

    PaymentIntent intent = new PaymentIntent();
    intent.setReference("PI-" + UUID.randomUUID());
    intent.setBookingReference(booking.reference());
    intent.setUserId(booking.requesterUserId());
    intent.setAmount(booking.serviceFee());
    intent.setCurrency("TZS");
    intent.setProvider(request.provider());
    intent.setPurpose(PaymentPurpose.VIEWING_FEE);
    intent.setStatus(PaymentIntentStatus.PENDING);
    intent.setReturnUrl(request.returnUrl());
    intent.setExpiresAt(LocalDateTime.now().plusMinutes(
      request.expiresInMinutes() != null ? request.expiresInMinutes() : DEFAULT_EXPIRY_MINUTES
    ));
    intent.setRedirectUrl(resolveAdapter(intent).buildRedirectUrl(intent));

    return toDetails(paymentIntentRepository.save(intent));
  }

  public PaymentIntentDetailsDTO getIntent(String reference) {
    return toDetails(findIntent(reference));
  }

  @Transactional
  public PaymentIntentDetailsDTO handleCallback(PaymentCallbackDTO callback) {
    PaymentIntent intent = findIntent(callback.reference());

    if (intent.getStatus() != PaymentIntentStatus.PENDING) {
      return toDetails(intent);
    }

    intent.setProviderTransactionId(callback.providerTransactionId());
    intent.setSettledAt(LocalDateTime.now());

    if (Boolean.TRUE.equals(callback.successful())) {
      settleSuccessfully(intent);
    } else {
      intent.setStatus(PaymentIntentStatus.FAILED);
      intent.setFailureReason(callback.failureReason());
    }

    intent = paymentIntentRepository.save(intent);
    reportToRentManager(intent, intent.getStatus() == PaymentIntentStatus.SUCCESSFUL ? "PAID" : "FAILED", intent.getFailureReason());
    return toDetails(intent);
  }

  @Transactional
  public PaymentIntentDetailsDTO refundBookingPayment(String bookingReference, String reason) {
    PaymentIntent intent = paymentIntentRepository
      .findFirstByBookingReferenceAndStatusOrderByIdDesc(bookingReference, PaymentIntentStatus.SUCCESSFUL)
      .orElseThrow(() -> new ResourceNotFoundException("No settled payment found for booking " + bookingReference + "."));

    if (intent.isRefunded()) {
      return toDetails(intent);
    }

    virtualAccountService.credit(intent.getUserId(), new VirtualAccountTransactionDTO(
      intent.getAmount(),
      VirtualAccountEntryType.VIEWING_FEE_REFUND,
      intent.getReference() + "-REFUND",
      reason != null ? reason : "Refund for booking " + bookingReference
    ));

    intent.setRefunded(true);
    intent.setRefundedAt(LocalDateTime.now());
    intent = paymentIntentRepository.save(intent);

    reportToRentManager(intent, "REFUNDED", reason);
    return toDetails(intent);
  }

  @Transactional
  public int expireStaleIntents() {
    List<PaymentIntent> staleIntents = paymentIntentRepository
      .findByStatusAndExpiresAtBefore(PaymentIntentStatus.PENDING, LocalDateTime.now());

    staleIntents.forEach(intent -> {
      intent.setStatus(PaymentIntentStatus.EXPIRED);
      intent.setFailureReason("Payment intent expired before settlement.");
      paymentIntentRepository.save(intent);
      reportToRentManager(intent, "FAILED", intent.getFailureReason());
    });

    return staleIntents.size();
  }

  private void settleSuccessfully(PaymentIntent intent) {
    intent.setStatus(PaymentIntentStatus.SUCCESSFUL);

    virtualAccountService.credit(intent.getUserId(), new VirtualAccountTransactionDTO(
      intent.getAmount(),
      VirtualAccountEntryType.TOP_UP,
      intent.getReference() + "-TOPUP",
      "Provider payment via " + intent.getProvider().name()
    ));

    virtualAccountService.debit(intent.getUserId(), new VirtualAccountTransactionDTO(
      intent.getAmount(),
      VirtualAccountEntryType.VIEWING_FEE_PAYMENT,
      intent.getReference() + "-FEE",
      "Viewing fee for booking " + intent.getBookingReference()
    ));
  }

  private void reportToRentManager(PaymentIntent intent, String paymentStatus, String reason) {
    if (intent.getPurpose() != PaymentPurpose.VIEWING_FEE || intent.getBookingReference() == null) {
      return;
    }

    rentManagerClient.updateViewingPaymentStatus(intent.getBookingReference(), paymentStatus, reason);
  }

  private PaymentProviderAdapter resolveAdapter(PaymentIntent intent) {
    return providerAdapters.stream()
      .filter(adapter -> adapter.supports(intent.getProvider()))
      .findFirst()
      .orElseThrow(() -> new TantePayException("No payment adapter available for provider " + intent.getProvider()));
  }

  private PaymentIntent findIntent(String reference) {
    return paymentIntentRepository.findByReference(reference)
      .orElseThrow(() -> new ResourceNotFoundException("Payment intent " + reference + " not found."));
  }

  private PaymentIntentDetailsDTO toDetails(PaymentIntent intent) {
    return new PaymentIntentDetailsDTO(
      intent.getId(),
      intent.getReference(),
      intent.getBookingReference(),
      intent.getUserId(),
      intent.getAmount(),
      intent.getCurrency(),
      intent.getProvider(),
      intent.getPurpose(),
      intent.getStatus(),
      intent.getRedirectUrl(),
      intent.getProviderTransactionId(),
      intent.getFailureReason(),
      intent.isRefunded(),
      intent.getExpiresAt(),
      intent.getSettledAt()
    );
  }
}
