package pay.tante.tz.models.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pay.tante.tz.enums.TanteService;

import java.math.BigDecimal;

public record CreateGenericPaymentIntentDTO(
  @NotNull(message = "User Account ID is required")
  Long userAccountId,

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
  BigDecimal amount,

  @NotBlank(message = "Currency is required")
  String currency,

  @NotNull(message = "Service is required")
  TanteService service,

  @NotBlank(message = "Service Reference Number is required")
  String serviceReferenceNumber,

  @NotBlank(message = "Status Callback URL is required")
  String callbackUrl
) {
}
