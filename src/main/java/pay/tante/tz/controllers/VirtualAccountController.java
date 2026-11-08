package pay.tante.tz.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pay.tante.tz.models.dtos.ApiResponse;
import pay.tante.tz.models.dtos.requests.VirtualAccountTransactionDTO;
import pay.tante.tz.models.dtos.responses.VirtualAccountDetailsDTO;
import pay.tante.tz.models.dtos.responses.VirtualAccountEntryDetailsDTO;
import pay.tante.tz.services.VirtualAccountService;

import java.util.List;

@RestController
@RequestMapping("/v1/virtual-accounts")
@AllArgsConstructor
public class VirtualAccountController {
  private final VirtualAccountService virtualAccountService;

  @PostMapping("/{userId}")
  public ResponseEntity<ApiResponse<VirtualAccountDetailsDTO>> createVirtualAccount(@PathVariable Long userId) {
    VirtualAccountDetailsDTO response = virtualAccountService.getOrCreateAccount(userId);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(response, HttpStatus.CREATED.value()));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<ApiResponse<VirtualAccountDetailsDTO>> getVirtualAccount(@PathVariable Long userId) {
    VirtualAccountDetailsDTO response = virtualAccountService.getAccount(userId);
    return ResponseEntity.status(HttpStatus.OK)
      .body(ApiResponse.success(response, HttpStatus.OK.value()));
  }

  @GetMapping("/{userId}/entries")
  public ResponseEntity<ApiResponse<List<VirtualAccountEntryDetailsDTO>>> getVirtualAccountEntries(@PathVariable Long userId) {
    List<VirtualAccountEntryDetailsDTO> response = virtualAccountService.getEntries(userId);
    return ResponseEntity.status(HttpStatus.OK)
      .body(ApiResponse.success(response, HttpStatus.OK.value()));
  }

  @PostMapping("/{userId}/top-up")
  public ResponseEntity<ApiResponse<VirtualAccountDetailsDTO>> topUpVirtualAccount(
    @PathVariable Long userId,
    @Valid @RequestBody VirtualAccountTransactionDTO request
  ) {
    VirtualAccountDetailsDTO response = virtualAccountService.topUp(userId, request);
    return ResponseEntity.status(HttpStatus.OK)
      .body(ApiResponse.success(response, HttpStatus.OK.value()));
  }

  @PostMapping("/{userId}/debit")
  public ResponseEntity<ApiResponse<VirtualAccountDetailsDTO>> debitVirtualAccount(
    @PathVariable Long userId,
    @Valid @RequestBody VirtualAccountTransactionDTO request
  ) {
    VirtualAccountDetailsDTO response = virtualAccountService.debit(userId, request);
    return ResponseEntity.status(HttpStatus.OK)
      .body(ApiResponse.success(response, HttpStatus.OK.value()));
  }

  @PostMapping("/{userId}/credit")
  public ResponseEntity<ApiResponse<VirtualAccountDetailsDTO>> creditVirtualAccount(
    @PathVariable Long userId,
    @Valid @RequestBody VirtualAccountTransactionDTO request
  ) {
    VirtualAccountDetailsDTO response = virtualAccountService.credit(userId, request);
    return ResponseEntity.status(HttpStatus.OK)
      .body(ApiResponse.success(response, HttpStatus.OK.value()));
  }
}
