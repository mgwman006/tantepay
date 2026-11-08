package pay.tante.tz.providers;

import pay.tante.tz.models.entities.PaymentIntent;

public interface PaymentProviderAdapter {
  boolean supports(pay.tante.tz.enums.PaymentProvider provider);

  String buildRedirectUrl(PaymentIntent intent);
}
