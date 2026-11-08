package pay.tante.tz.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentIntentExpiryScheduler {
  private static final Logger LOGGER = LoggerFactory.getLogger(PaymentIntentExpiryScheduler.class);

  private final PaymentIntentService paymentIntentService;

  @Scheduled(fixedDelayString = "${tantepay.payment-intent.expiry-scan-ms:60000}")
  public void expireStaleIntents() {
    int expired = paymentIntentService.expireStaleIntents();
    if (expired > 0) {
      LOGGER.info("Expired {} stale payment intents.", expired);
    }
  }
}
