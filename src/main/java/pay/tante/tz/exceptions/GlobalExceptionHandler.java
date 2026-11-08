package pay.tante.tz.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pay.tante.tz.models.dtos.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
      .body(ApiResponse.failure(exception.getMessage(), HttpStatus.NOT_FOUND.value()));
  }

  @ExceptionHandler(InsufficientBalanceException.class)
  public ResponseEntity<ApiResponse<Void>> handleInsufficientBalance(InsufficientBalanceException exception) {
    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
      .body(ApiResponse.failure(exception.getMessage(), HttpStatus.PAYMENT_REQUIRED.value()));
  }

  @ExceptionHandler(TantePayException.class)
  public ResponseEntity<ApiResponse<Void>> handleTantePay(TantePayException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(ApiResponse.failure(exception.getMessage(), HttpStatus.BAD_REQUEST.value()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream()
      .findFirst()
      .map(error -> error.getField() + ": " + error.getDefaultMessage())
      .orElse("Validation failed");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(ApiResponse.failure(message, HttpStatus.BAD_REQUEST.value()));
  }
}
