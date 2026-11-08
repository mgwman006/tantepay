package pay.tante.tz.models.dtos.responses;

import java.math.BigDecimal;

public record VirtualAccountDetailsDTO(
  Long id,
  Long userId,
  BigDecimal balance,
  String currency,
  boolean active
) {
}
