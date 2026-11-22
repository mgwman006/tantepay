package pay.tante.tz.providers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pay.tante.tz.enums.PaymentProvider;
import pay.tante.tz.models.entities.PaymentIntent;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class HostedCheckoutAdapter implements PaymentProviderAdapter {
  private final String checkoutBaseUrl;

  public HostedCheckoutAdapter(@Value("${tantepay.checkout.base-url:http://localhost:4179/pay}") String checkoutBaseUrl) {
    this.checkoutBaseUrl = checkoutBaseUrl;
  }

  @Override
  public boolean supports(PaymentProvider provider) {
    return true;
  }

  @Override
  public String buildRedirectUrl(PaymentIntent intent) {
    String normalizedBase = checkoutBaseUrl.endsWith("/")
      ? checkoutBaseUrl.substring(0, checkoutBaseUrl.length() - 1)
      : checkoutBaseUrl;
    return normalizedBase + "/" + encode(intent.getId().toString()) + "?token=" + encode(intent.getServiceReferenceNumber());
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
