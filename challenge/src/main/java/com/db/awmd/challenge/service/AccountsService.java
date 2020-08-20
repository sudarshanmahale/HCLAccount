package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferBetweenAccount;
import com.db.awmd.challenge.exception.AccountNotNotFoundException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundException;
import com.db.awmd.challenge.repository.AccountsRepository;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public Boolean transferAmountBetweenAccounts(final TransferBetweenAccount transferAccountDetails)
      throws AccountNotNotFoundException, InsufficientFundException {

    Boolean success = Boolean.FALSE;
    final String fromAccountNumber = transferAccountDetails.getAccountFrom();
    final String toAccountNumber = transferAccountDetails.getAccountTo();

    final BigDecimal amountToTransfer = transferAccountDetails.getTransferAmount();
    final Account formAccount = getAccount(fromAccountNumber);
    final Account toAccount = getAccount(toAccountNumber);

    if (Objects.isNull(formAccount)) {
      throw new AccountNotNotFoundException(
          "Debited account not found " + transferAccountDetails.getAccountFrom());
    }

    if (Objects.isNull(toAccount)) {
      throw new AccountNotNotFoundException(
          "Deposited account not found " + toAccountNumber);
    }

    if ((formAccount.getBalance().compareTo(amountToTransfer)) < 0) {
      throw new InsufficientFundException(
          "Insufficient fund in account " + transferAccountDetails.getAccountTo());
    }

    if ((fromAccountNumber.equals(toAccountNumber))) {
      throw new DuplicateAccountIdException(
          "Fund cannot be transfer in same account " + transferAccountDetails.getAccountTo());
    }

    log.info(
        "Account transfer validation completed for from account {} with amount of {} transfer process to account number {} ",
        fromAccountNumber, amountToTransfer, toAccount);

    if (withdraw(formAccount, amountToTransfer)) {
      success = deposit(toAccount, amountToTransfer);
    }

    return success;
  }

  private synchronized Boolean withdraw(Account fromAccount, BigDecimal amount) {
    Boolean success = Boolean.FALSE;

    if (fromAccount.getBalance().compareTo(amount) >= 0) {
      log.info(
          "Processing for withdraw from account number  {} with amount of {}",
          fromAccount, amount);

      fromAccount.setBalance(fromAccount.getBalance().subtract(amount));

      success = this.accountsRepository.updateAccount(fromAccount);
    }

    log.info(
        "Processed withdraw from account number  {} with amount of {}",
        fromAccount, amount);

    return success;
  }

  private synchronized Boolean deposit(Account toAccount, BigDecimal amount) {
    toAccount.setBalance(toAccount.getBalance().add(amount));
    return this.accountsRepository.updateAccount(toAccount);
  }

}
