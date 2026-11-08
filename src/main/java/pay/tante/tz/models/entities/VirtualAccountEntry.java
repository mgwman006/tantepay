package pay.tante.tz.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pay.tante.tz.enums.VirtualAccountEntryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
  name = "virtual_account_entries",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_virtual_account_entry_reference", columnNames = {"reference"})
  },
  indexes = {
    @Index(name = "idx_virtual_account_entry_account", columnList = "virtual_account_id")
  }
)
public class VirtualAccountEntry extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "virtual_account_id", nullable = false)
  private VirtualAccount virtualAccount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private VirtualAccountEntryType type;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
  private BigDecimal balanceAfter;

  @Column(length = 100)
  private String reference;

  @Column(length = 255)
  private String description;

  @Column(name = "entry_date", nullable = false)
  private LocalDateTime entryDate;
}
