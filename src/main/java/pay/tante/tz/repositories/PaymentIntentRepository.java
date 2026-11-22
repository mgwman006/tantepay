package pay.tante.tz.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import pay.tante.tz.models.entities.PaymentIntent;


@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {
  Optional<PaymentIntent> findByServiceReferenceNumber(String serviceReferenceNumber);
}
