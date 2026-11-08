package pay.tante.tz.models.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pay.tante.tz.enums.PaymentProvider;

public record CreatePaymentIntentDTO(
  @NotBlank(message = "Booking reference is required") String bookingReference,
  @NotNull(message = "Payment provider is required") PaymentProvider provider,
  String returnUrl,
  Integer expiresInMinutes
) {
}
