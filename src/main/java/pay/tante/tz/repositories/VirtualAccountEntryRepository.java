package pay.tante.tz.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualAccountEntryRepository extends JpaRepository<VirtualAccountEntry, Long> {
  List<VirtualAccountEntry> findByVirtualAccountIdOrderByEntryDateDesc(Long virtualAccountId);

  Optional<VirtualAccountEntry> findByReference(String reference);
}
