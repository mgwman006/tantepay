package pay.tante.tz.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pay.tante.tz.enums.PaymentIntentStatus;
import pay.tante.tz.enums.TanteService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payment_intents")
public class PaymentIntent extends BaseEntity {

  @Column(nullable = false)
  Long userAccountId;

  @Column(nullable = false, length = 100)
  TanteService service;

  @Column(nullable = false)
  private String serviceReferenceNumber;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency = "TZS";

  @Column(nullable = false)
  private String statusCallbackUrl;

  @Column(nullable = false)
  private String paymentLink;

  @Enumerated(EnumType.STRING)
  PaymentIntentStatus status = PaymentIntentStatus.PENDING;

  @Column(nullable = false)
  LocalDateTime createdAt;

  @Column(nullable = false)
  LocalDateTime expiresAt;

  LocalDateTime paidAt;

  LocalDateTime refundedAt;

  @OneToMany(mappedBy = "paymentIntent", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<PaymentTransaction> paymentTransactions = new ArrayList<>();

  public void addPaymentTransaction(PaymentTransaction paymentTransaction)
  {
    paymentTransactions.add(paymentTransaction);
    paymentTransaction.setPaymentIntent(this);
  }
}