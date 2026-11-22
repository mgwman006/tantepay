package pay.tante.tz.models.dtos.responses;

import jakarta.persistence.Column;
import pay.tante.tz.enums.PaymentIntentStatus;
import pay.tante.tz.enums.PaymentProvider;
import pay.tante.tz.enums.TanteService;
import pay.tante.tz.models.entities.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentIntentDetailsDTO(
  Long id,
  Long userAccountId,
  TanteService service,
  String serviceReferenceNumber,
  BigDecimal amount,
  String currency,
  String statusCallbackUrl,
  String paymentLink,
  PaymentIntentStatus status,
  LocalDateTime createdAt,
  LocalDateTime expiresAt,
  LocalDateTime paidAt,
  LocalDateTime refundedAt
)
{
}