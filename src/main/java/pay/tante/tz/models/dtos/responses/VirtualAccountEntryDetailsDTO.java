package pay.tante.tz.models.dtos.responses;

import pay.tante.tz.enums.VirtualAccountEntryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VirtualAccountEntryDetailsDTO(
  Long id,
  Long virtualAccountId,
  VirtualAccountEntryType type,
  BigDecimal amount,
  BigDecimal balanceAfter,
  String reference,
  String description,
  LocalDateTime entryDate
) {
}
