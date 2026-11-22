package pay.tante.tz.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pay.tante.tz.models.entities.PaymentTransaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
  Optional<PaymentTransaction> findByReference(String reference);
  Optional<PaymentTransaction> findByProviderTransactionId(String providerTransactionId);
  List<PaymentTransaction> findByPaymentIntentIdOrderByInitiatedAtDesc(Long paymentIntentId);
}
