package pay.tante.tz.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "virtual_accounts")
public class VirtualAccount extends BaseEntity {
  @Column(nullable = false)
  private Long userAccountId;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal balance = BigDecimal.ZERO;

  @Column(nullable = false, length = 3)
  private String currency = "TZS";

  @Column(nullable = false)
  private boolean active = true;

  @OneToMany(mappedBy = "virtualAccount", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<VirtualAccountEntry> entries = new ArrayList<>();
}
