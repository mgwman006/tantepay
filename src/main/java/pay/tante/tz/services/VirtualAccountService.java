package pay.tante.tz.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pay.tante.tz.enums.VirtualAccountEntryType;
import pay.tante.tz.exceptions.InsufficientBalanceException;
import pay.tante.tz.exceptions.ResourceNotFoundException;
import pay.tante.tz.exceptions.TantePayException;
import pay.tante.tz.models.dtos.requests.VirtualAccountTransactionDTO;
import pay.tante.tz.models.dtos.responses.VirtualAccountDetailsDTO;
import pay.tante.tz.models.dtos.responses.VirtualAccountEntryDetailsDTO;
import pay.tante.tz.models.entities.VirtualAccount;
import pay.tante.tz.models.entities.VirtualAccountEntry;
import pay.tante.tz.repositories.VirtualAccountEntryRepository;
import pay.tante.tz.repositories.VirtualAccountRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class VirtualAccountService {
  private final VirtualAccountRepository virtualAccountRepository;
  private final VirtualAccountEntryRepository virtualAccountEntryRepository;

  @Transactional
  public VirtualAccountDetailsDTO getOrCreateAccount(Long userId) {
    if (userId == null) {
      throw new TantePayException("User ID is required to resolve a virtual account.");
    }

    VirtualAccount account = virtualAccountRepository.findByUserId(userId)
      .orElseGet(() -> {
        VirtualAccount newAccount = new VirtualAccount();
        newAccount.setUserId(userId);
        newAccount.setBalance(BigDecimal.ZERO);
        return virtualAccountRepository.save(newAccount);
      });

    return toAccountDetails(account);
  }

  public VirtualAccountDetailsDTO getAccount(Long userId) {
    return toAccountDetails(findAccount(userId));
  }

  public List<VirtualAccountEntryDetailsDTO> getEntries(Long userId) {
    VirtualAccount account = findAccount(userId);
    return virtualAccountEntryRepository.findByVirtualAccountIdOrderByEntryDateDesc(account.getId()).stream()
      .map(this::toEntryDetails)
      .toList();
  }

  @Transactional
  public VirtualAccountDetailsDTO topUp(Long userId, VirtualAccountTransactionDTO request) {
    return credit(userId, new VirtualAccountTransactionDTO(
      request.amount(),
      VirtualAccountEntryType.TOP_UP,
      request.reference(),
      request.description() != null ? request.description() : "Virtual account top up"
    ));
  }

  @Transactional
  public VirtualAccountDetailsDTO debit(Long userId, VirtualAccountTransactionDTO request) {
    validateAmount(request.amount());

    VirtualAccountEntry existingEntry = findExistingEntry(request.reference());
    if (existingEntry != null) {
      return toAccountDetails(existingEntry.getVirtualAccount());
    }

    getOrCreateAccount(userId);
    VirtualAccount account = lockAccount(userId);

    if (!account.isActive()) {
      throw new TantePayException("This virtual account is not active.");
    }

    if (account.getBalance().compareTo(request.amount()) < 0) {
      throw new InsufficientBalanceException("Insufficient virtual account balance. Please top up before proceeding.");
    }

    account.setBalance(account.getBalance().subtract(request.amount()));
    account = virtualAccountRepository.save(account);
    recordEntry(account, resolveType(request.type(), VirtualAccountEntryType.ADJUSTMENT), request.amount().negate(), request.reference(), request.description());
    return toAccountDetails(account);
  }

  @Transactional
  public VirtualAccountDetailsDTO credit(Long userId, VirtualAccountTransactionDTO request) {
    validateAmount(request.amount());

    VirtualAccountEntry existingEntry = findExistingEntry(request.reference());
    if (existingEntry != null) {
      return toAccountDetails(existingEntry.getVirtualAccount());
    }

    getOrCreateAccount(userId);
    VirtualAccount account = lockAccount(userId);
    account.setBalance(account.getBalance().add(request.amount()));
    account = virtualAccountRepository.save(account);
    recordEntry(account, resolveType(request.type(), VirtualAccountEntryType.ADJUSTMENT), request.amount(), request.reference(), request.description());
    return toAccountDetails(account);
  }

  private VirtualAccountEntryType resolveType(VirtualAccountEntryType type, VirtualAccountEntryType fallback) {
    return type != null ? type : fallback;
  }

  private VirtualAccountEntry findExistingEntry(String reference) {
    if (reference == null || reference.isBlank()) {
      return null;
    }
    return virtualAccountEntryRepository.findByReference(reference).orElse(null);
  }

  private VirtualAccount findAccount(Long userId) {
    return virtualAccountRepository.findByUserId(userId)
      .orElseThrow(() -> new ResourceNotFoundException("Virtual account for user " + userId + " not found."));
  }

  private VirtualAccount lockAccount(Long userId) {
    return virtualAccountRepository.findByUserIdForUpdate(userId)
      .orElseThrow(() -> new ResourceNotFoundException("Virtual account for user " + userId + " not found."));
  }

  private void validateAmount(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new TantePayException("Virtual account amount must be greater than zero.");
    }
  }

  private void recordEntry(VirtualAccount account, VirtualAccountEntryType type, BigDecimal amount, String reference, String description) {
    VirtualAccountEntry entry = new VirtualAccountEntry();
    entry.setVirtualAccount(account);
    entry.setType(type);
    entry.setAmount(amount);
    entry.setBalanceAfter(account.getBalance());
    entry.setReference(reference);
    entry.setDescription(description);
    entry.setEntryDate(LocalDateTime.now());
    virtualAccountEntryRepository.save(entry);
  }

  private VirtualAccountDetailsDTO toAccountDetails(VirtualAccount account) {
    return new VirtualAccountDetailsDTO(
      account.getId(),
      account.getUserId(),
      account.getBalance(),
      account.getCurrency(),
      account.isActive()
    );
  }

  private VirtualAccountEntryDetailsDTO toEntryDetails(VirtualAccountEntry entry) {
    return new VirtualAccountEntryDetailsDTO(
      entry.getId(),
      entry.getVirtualAccount() != null ? entry.getVirtualAccount().getId() : null,
      entry.getType(),
      entry.getAmount(),
      entry.getBalanceAfter(),
      entry.getReference(),
      entry.getDescription(),
      entry.getEntryDate()
    );
  }
}
