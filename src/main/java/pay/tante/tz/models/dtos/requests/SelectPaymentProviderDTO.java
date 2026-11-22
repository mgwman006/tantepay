package pay.tante.tz.models.dtos.requests;

import jakarta.validation.constraints.NotNull;
import pay.tante.tz.enums.PaymentProvider;

public record SelectPaymentProviderDTO(
  @NotNull(message = "Payment provider is required") PaymentProvider provider,
  String phoneNumber
) {
}
