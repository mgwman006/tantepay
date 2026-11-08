package pay.tante.tz.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pay.tante.tz.enums.PaymentIntentStatus;
import pay.tante.tz.models.entities.PaymentIntent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {
  Optional<PaymentIntent> findByReference(String reference);

  List<PaymentIntent> findByStatusAndExpiresAtBefore(PaymentIntentStatus status, LocalDateTime cutoff);

  Optional<PaymentIntent> findFirstByBookingReferenceAndStatusOrderByIdDesc(String bookingReference, PaymentIntentStatus status);

  Optional<PaymentIntent> findFirstByBookingReferenceAndStatusInOrderByIdDesc(String bookingReference, java.util.Collection<PaymentIntentStatus> statuses);
}
