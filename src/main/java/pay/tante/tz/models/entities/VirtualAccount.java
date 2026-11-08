package pay.tante.tz.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
  name = "virtual_accounts",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_virtual_account_user", columnNames = {"user_id"})
  }
)
public class VirtualAccount extends BaseEntity {
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal balance = BigDecimal.ZERO;

  @Column(nullable = false, length = 3)
  private String currency = "TZS";

  @Column(nullable = false)
  private boolean active = true;
}
