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

  public HostedCheckoutAdapter(@Value("${tantepay.checkout.base-url:http://localhost:8085/checkout}") String checkoutBaseUrl) {
    this.checkoutBaseUrl = checkoutBaseUrl;
  }

  @Override
  public boolean supports(PaymentProvider provider) {
    return true;
  }

  @Override
  public String buildRedirectUrl(PaymentIntent intent) {
    StringBuilder url = new StringBuilder(checkoutBaseUrl)
      .append("?reference=").append(encode(intent.getReference()))
      .append("&provider=").append(intent.getProvider().name())
      .append("&amount=").append(intent.getAmount().toPlainString())
      .append("&currency=").append(encode(intent.getCurrency()));

    if (intent.getReturnUrl() != null) {
      url.append("&returnUrl=").append(encode(intent.getReturnUrl()));
    }

    return url.toString();
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
