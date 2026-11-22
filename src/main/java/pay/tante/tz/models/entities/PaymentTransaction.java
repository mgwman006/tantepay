package pay.tante.tz.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pay.tante.tz.enums.PaymentProvider;
import pay.tante.tz.enums.PaymentTransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction extends BaseEntity {

  @Column(nullable = false, length = 100)
  private String reference;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PaymentProvider provider;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PaymentTransactionStatus status = PaymentTransactionStatus.PENDING;

  @Column
  private String providerTransactionId;

  @Column
  private Long virtualAccountId;

  @Column(length = 1000)
  private String failureReason;

  @Column(nullable = false)
  private LocalDateTime initiatedAt;

  @Column
  private LocalDateTime processedAt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "payment_intent_id", nullable = false)
  private PaymentIntent paymentIntent;
}
