package pay.tante.tz.models.dtos.responses;

import java.math.BigDecimal;

public record ViewingBookingPaymentDTO(
  String reference,
  Long requesterUserId,
  BigDecimal serviceFee,
  String status,
  String paymentStatus
) {
}
