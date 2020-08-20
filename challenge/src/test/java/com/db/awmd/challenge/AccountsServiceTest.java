package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferBetweenAccount;
import com.db.awmd.challenge.exception.AccountNotNotFoundException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  public static final String ACC_457412 = "Acc-457412";
  public static final String ACC_452112 = "ACC-452112";
  public static final String ACC_457413 = "Acc-457413";
  public static final String ACC_452113 = "ACC-452113";
  public static final String ACC_452114 = "ACC-452114";
  public static final String ACC_457414 = "ACC-459113";


  @Autowired
  private AccountsService accountsService;

  @Mock
  private AccountsRepository accountsRepository;

  @Before()
  public void initMock() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);
    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);

  }

  private void createAccount(final String accountNumber,final BigDecimal amount){
    Account account = new Account(accountNumber);
    account.setBalance(amount);
    this.accountsService.createAccount(account);
    assertThat(this.accountsService.getAccount(accountNumber)).isEqualTo(account);
  }


  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  public void transferAmountBetweenAccounts_DebitedNotFound()
      throws AccountNotNotFoundException, InsufficientFundException {

    final String uniqueId = "Id-" + System.currentTimeMillis();
    final TransferBetweenAccount transferAccountDetails = new TransferBetweenAccount(uniqueId,
        "1234", new BigDecimal(4000));

    try {
      this.accountsService.transferAmountBetweenAccounts(transferAccountDetails);
    } catch (AccountNotNotFoundException anfe) {
      assertThat(anfe.getMessage())
          .isEqualTo("Debited account not found " + transferAccountDetails.getAccountFrom());
    }
  }

  @Test
  public void transferAmountBetweenAccounts_DepositedNotFound()
      throws AccountNotNotFoundException, InsufficientFundException {

    final String uniqueId = "Id-" + System.currentTimeMillis();
    createAccount(ACC_457412,new BigDecimal(5000));
    createAccount(ACC_452112,new BigDecimal(10000));

    final TransferBetweenAccount transferAccountDetails = new TransferBetweenAccount(ACC_452112,
        ACC_457412, new BigDecimal(4000));

    try {
      this.accountsService.transferAmountBetweenAccounts(transferAccountDetails);
    } catch (AccountNotNotFoundException anfe) {
      assertThat(anfe.getMessage())
          .isEqualTo("Deposited account not found " + transferAccountDetails.getAccountTo());
    }
  }

  @Test
  public void transferAmountBetweenAccounts_InsufficientFund()
      throws AccountNotNotFoundException, InsufficientFundException {

    final String uniqueId = "Id-" + System.currentTimeMillis();
    createAccount(ACC_457413,new BigDecimal(5000));
    createAccount(ACC_452113,new BigDecimal(5000));

    final TransferBetweenAccount transferAccountDetails = new TransferBetweenAccount(ACC_452113,
        ACC_457413, new BigDecimal(6000));

    try {
      this.accountsService.transferAmountBetweenAccounts(transferAccountDetails);
    } catch (InsufficientFundException ife) {
      assertThat(ife.getMessage())
          .isEqualTo("Insufficient fund in account " + transferAccountDetails.getAccountFrom());
    }
  }


  @Test
  public void transferAmountBetweenAccounts_SuccessTransaction()
      throws AccountNotNotFoundException, InsufficientFundException {

    final String uniqueId = "Id-" + System.currentTimeMillis();
    createAccount(ACC_457414,new BigDecimal(5000));
    createAccount(ACC_452114,new BigDecimal(5000));

    final TransferBetweenAccount transferAccountDetails = new TransferBetweenAccount(ACC_452114,
        ACC_457414, new BigDecimal(5000));

      assertTrue(this.accountsService.transferAmountBetweenAccounts(transferAccountDetails));
  }

  @Test
  public void transferAmountBetweenAccounts_SuccessTransactionAmountCheck()
      throws AccountNotNotFoundException, InsufficientFundException {

    final String uniqueIdToAccount = "IdTo-" + System.currentTimeMillis();
    final String uniqueIdFromAccount = "IdFrom-" + System.currentTimeMillis();
    createAccount(uniqueIdToAccount,new BigDecimal(5000));
    createAccount(uniqueIdFromAccount,new BigDecimal(5000));

    final TransferBetweenAccount transferAccountDetails = new TransferBetweenAccount(uniqueIdFromAccount,
        uniqueIdToAccount, new BigDecimal(5000));

    assertTrue(this.accountsService.transferAmountBetweenAccounts(transferAccountDetails));
    Account account = this.accountsService.getAccount(uniqueIdFromAccount);
    assertEquals(new BigDecimal(0),account.getBalance());
  }

}
