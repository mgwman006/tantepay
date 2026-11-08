package pay.tante.tz.models.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCallbackDTO(
  @NotBlank(message = "Payment reference is required") String reference,
  @NotNull(message = "Payment result is required") Boolean successful,
  String providerTransactionId,
  String failureReason
) {
}
