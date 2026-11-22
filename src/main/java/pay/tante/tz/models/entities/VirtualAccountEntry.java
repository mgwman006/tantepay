package pay.tante.tz.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pay.tante.tz.enums.VirtualAccountEntryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "virtual_account_entries")
public class VirtualAccountEntry extends BaseEntity
{
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal balanceAfter;

  @Column(nullable = false)
  private String reference;

  private String description;

  @Column(nullable = false)
  private LocalDateTime entryDate;

  @Enumerated(EnumType.STRING)
  private VirtualAccountEntryType type;

  @ManyToOne(fetch = FetchType.LAZY)
  private VirtualAccount virtualAccount;

}
