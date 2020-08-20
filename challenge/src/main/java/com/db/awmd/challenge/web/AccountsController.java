package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferBetweenAccount;
import com.db.awmd.challenge.exception.AccountNotNotFoundException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundException;
import com.db.awmd.challenge.service.AccountsService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE ,path = "/transfer")
  public ResponseEntity<Object> transferMoney(@RequestBody @Valid TransferBetweenAccount accountDetails) {
    log.info("Transfer amount between account details {}", accountDetails);

    try {
      this.accountsService.transferAmountBetweenAccounts(accountDetails);
    } catch (AccountNotNotFoundException anfe) {
      return new ResponseEntity<>(anfe.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (InsufficientFundException ife) {
      return new ResponseEntity<>(ife.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (DuplicateAccountIdException dae) {
      return new ResponseEntity<>(dae.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }
}