package pay.tante.tz.models.dtos.responses;

import pay.tante.tz.enums.PaymentIntentStatus;
import pay.tante.tz.enums.TanteService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentIntentDetailsDTO(
  Long id,
  Long userAccountId,
  TanteService service,
  String serviceReferenceNumber,
  BigDecimal amount,
  String currency,
  String callbackUrl,
  String paymentLink,
  PaymentIntentStatus status,
  LocalDateTime createdAt,
  LocalDateTime expiresAt,
  LocalDateTime paidAt,
  LocalDateTime refundedAt
)
{
}