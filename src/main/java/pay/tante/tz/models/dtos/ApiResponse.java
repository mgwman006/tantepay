package pay.tante.tz.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private int statusCode;

  public static <T> ApiResponse<T> success(T data, int statusCode) {
    return new ApiResponse<>(true, "Request successful", data, statusCode);
  }

  public static <T> ApiResponse<T> failure(String message, int statusCode) {
    return new ApiResponse<>(false, message, null, statusCode);
  }
}
