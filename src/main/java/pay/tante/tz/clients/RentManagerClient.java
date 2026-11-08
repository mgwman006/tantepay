package pay.tante.tz.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pay.tante.tz.exceptions.ResourceNotFoundException;
import pay.tante.tz.exceptions.TantePayException;
import pay.tante.tz.models.dtos.responses.ViewingBookingPaymentDTO;

import java.util.Map;

@Component
public class RentManagerClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(RentManagerClient.class);

  private final RestClient restClient;

  public RentManagerClient(
    RestClient.Builder restClientBuilder,
    @Value("${rent.manager.base.url:http://localhost:8083/rent-manager}") String baseUrl
  ) {
    this.restClient = restClientBuilder.baseUrl(baseUrl).build();
  }

  public ViewingBookingPaymentDTO fetchViewingBooking(String bookingReference) {
    try {
      RentManagerResponse<ViewingBookingPaymentDTO> response = restClient.get()
        .uri("/v1/viewings/bookings/by-reference/{reference}", bookingReference)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {
        });

      if (response == null || response.data() == null) {
        throw new ResourceNotFoundException("Viewing booking " + bookingReference + " not found.");
      }

      return response.data();
    } catch (ResourceNotFoundException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new TantePayException("Could not load viewing booking " + bookingReference + ": " + exception.getMessage());
    }
  }

  public void updateViewingPaymentStatus(String bookingReference, String paymentStatus, String reason) {
    try {
      restClient.patch()
        .uri("/v1/viewings/bookings/by-reference/{reference}/payment-status", bookingReference)
        .body(Map.of(
          "paymentStatus", paymentStatus,
          "reason", reason != null ? reason : ""
        ))
        .retrieve()
        .toBodilessEntity();
    } catch (Exception exception) {
      LOGGER.error("Failed to report payment status {} for booking {}: {}", paymentStatus, bookingReference, exception.getMessage());
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RentManagerResponse<T>(boolean success, String message, T data, int statusCode) {
  }
}
