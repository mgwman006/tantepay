package pay.tante.tz.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pay.tante.tz.enums.TanteService;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
  name = "payment_intents",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_payment_intent_reference", columnNames = {"reference"})
  },
  indexes = {
    @Index(name = "idx_payment_intent_user", columnList = "user_id"),
    @Index(name = "idx_payment_intent_booking", columnList = "booking_reference"),
    @Index(name = "idx_payment_intent_status", columnList = "status")
  }
)
public class PaymentIntent extends BaseEntity {

  Long userAccountId;

  TanteService service;

  private String serviceReferenceNumber;

  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency = "TZS";

  private String statusCallbackUrl;

  boolean refunded;
  LocalDateTime createdAt;
  LocalDateTime expiresAt;
  LocalDateTime settledAt;
}