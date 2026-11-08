package pay.tante.tz.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pay.tante.tz.models.entities.VirtualAccount;

import java.util.Optional;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {
  Optional<VirtualAccount> findByUserId(Long userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select va from VirtualAccount va where va.userId = :userId")
  Optional<VirtualAccount> findByUserIdForUpdate(@Param("userId") Long userId);
}
