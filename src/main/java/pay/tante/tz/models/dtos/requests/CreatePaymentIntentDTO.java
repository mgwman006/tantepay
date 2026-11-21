package pay.tante.tz.models.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import pay.tante.tz.enums.PaymentProvider;

public record CreatePaymentIntentDTO(
  @NotBlank(message = "Booking reference is required") String bookingReference,
  PaymentProvider provider,
  String returnUrl,
  Integer expiresInMinutes
) {
}
