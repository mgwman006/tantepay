package pay.tante.tz.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import pay.tante.tz.models.dtos.ApiResponse;
import pay.tante.tz.models.dtos.requests.CreateGenericPaymentIntentDTO;
import pay.tante.tz.models.dtos.responses.PaymentIntentDetailsDTO;
import pay.tante.tz.services.PaymentIntentService;

@RestController
@RequestMapping("/v1/payments")
@AllArgsConstructor
public class PaymentIntentController {
  private final PaymentIntentService paymentIntentService;

  @PostMapping("/intents")
  public ResponseEntity<ApiResponse<PaymentIntentDetailsDTO>> createPaymentIntent(@Valid @RequestBody CreateGenericPaymentIntentDTO request) {
    PaymentIntentDetailsDTO response = paymentIntentService.createPaymentIntent(request);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(response, HttpStatus.CREATED.value()));
  }



  @GetMapping("/intents/{serviceReferenceNumber}")
  public ResponseEntity<ApiResponse<PaymentIntentDetailsDTO>> getPaymentIntent(@PathVariable String serviceReferenceNumber) {
    PaymentIntentDetailsDTO response = paymentIntentService.getIntent(serviceReferenceNumber);
    return ResponseEntity.status(HttpStatus.OK)
      .body(ApiResponse.success(response, HttpStatus.OK.value()));
  }

  @PostMapping("/intents/{reference}/pay/virtual-account")
  public ResponseEntity<ApiResponse<PaymentIntentDetailsDTO>> payWithVirtualAccount(@PathVariable String reference) {
    PaymentIntentDetailsDTO response = paymentIntentService.payWithVirtualAccount(reference);
    return ResponseEntity.status(HttpStatus.OK)
      .body(ApiResponse.success(response, HttpStatus.OK.value()));
  }

}
