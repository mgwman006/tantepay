package pay.tante.tz.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pay.tante.tz.models.dtos.ApiResponse;
import pay.tante.tz.models.dtos.requests.CreatePaymentIntentDTO;
import pay.tante.tz.models.dtos.requests.PaymentCallbackDTO;
import pay.tante.tz.models.dtos.responses.PaymentIntentDetailsDTO;
import pay.tante.tz.services.PaymentIntentService;

@RestController
@RequestMapping("/v1/payments")
@AllArgsConstructor
public class PaymentIntentController {
  private final PaymentIntentService paymentIntentService;

  @PostMapping("/viewings")
  public ResponseEntity<ApiResponse<PaymentIntentDetailsDTO>> payForViewingBooking(@Valid @RequestBody CreatePaymentIntentDTO request) {
    PaymentIntentDetailsDTO response = paymentIntentService.createViewingPayment(request);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(response, HttpStatus.CREATED.value()));
  }

  @GetMapping("/intents/{reference}")
  public ResponseEntity<ApiResponse<PaymentIntentDetailsDTO>> getPaymentIntent(@PathVariable String reference) {
    PaymentIntentDetailsDTO response = paymentIntentService.getIntent(reference);
    return ResponseEntity.status(HttpStatus.OK)
      .body(ApiResponse.success(response, HttpStatus.OK.value()));
  }

  @PostMapping("/intents/callbacks")
  public ResponseEntity<ApiResponse<PaymentIntentDetailsDTO>> handlePaymentCallback(@Valid @RequestBody PaymentCallbackDTO callback) {
    PaymentIntentDetailsDTO response = paymentIntentService.handleCallback(callback);
    return ResponseEntity.status(HttpStatus.OK)
      .body(ApiResponse.success(response, HttpStatus.OK.value()));
  }

  @PostMapping("/bookings/{bookingReference}/refunds")
  public ResponseEntity<ApiResponse<PaymentIntentDetailsDTO>> refundBookingPayment(
    @PathVariable String bookingReference,
    @RequestParam(value = "reason", required = false) String reason
  ) {
    PaymentIntentDetailsDTO response = paymentIntentService.refundBookingPayment(bookingReference, reason);
    return ResponseEntity.status(HttpStatus.OK)
      .body(ApiResponse.success(response, HttpStatus.OK.value()));
  }
}
