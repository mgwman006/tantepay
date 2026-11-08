package pay.tante.tz.models.dtos.responses;

import pay.tante.tz.enums.PaymentIntentStatus;
import pay.tante.tz.enums.PaymentProvider;
import pay.tante.tz.enums.PaymentPurpose;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentIntentDetailsDTO(
  Long id,
  String reference,
  String bookingReference,
  Long userId,
  BigDecimal amount,
  String currency,
  PaymentProvider provider,
  PaymentPurpose purpose,
  PaymentIntentStatus status,
  String redirectUrl,
  String providerTransactionId,
  String failureReason,
  boolean refunded,
  LocalDateTime expiresAt,
  LocalDateTime settledAt
) {
}
