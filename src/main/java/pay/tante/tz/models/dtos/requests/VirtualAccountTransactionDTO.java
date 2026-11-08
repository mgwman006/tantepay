package pay.tante.tz.models.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import pay.tante.tz.enums.VirtualAccountEntryType;

import java.math.BigDecimal;

public record VirtualAccountTransactionDTO(
  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
  BigDecimal amount,
  VirtualAccountEntryType type,
  String reference,
  String description
) {
}
