package pay.tante.tz.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pay.tante.tz.enums.PaymentIntentStatus;
import pay.tante.tz.enums.PaymentProvider;
import pay.tante.tz.enums.PaymentPurpose;

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
  @Column(nullable = false, unique = true, length = 100)
  private String reference;

  @Column(name = "booking_reference", length = 60)
  private String bookingReference;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency = "TZS";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PaymentProvider provider;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PaymentPurpose purpose;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentIntentStatus status = PaymentIntentStatus.PENDING;

  @Column(name = "redirect_url", length = 500)
  private String redirectUrl;

  @Column(name = "return_url", length = 500)
  private String returnUrl;

  @Column(name = "callback_url", length = 500)
  private String callbackUrl;

  @Column(name = "provider_transaction_id", length = 100)
  private String providerTransactionId;

  @Column(name = "failure_reason", length = 255)
  private String failureReason;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  @Column(name = "settled_at")
  private LocalDateTime settledAt;

  @Column(name = "refunded", nullable = false)
  private boolean refunded = false;

  @Column(name = "refunded_at")
  private LocalDateTime refundedAt;
}
