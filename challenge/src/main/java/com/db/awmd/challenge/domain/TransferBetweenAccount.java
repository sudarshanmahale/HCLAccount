package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class TransferBetweenAccount {

  @NotNull
  @NotEmpty
  private final String accountFrom;

  @NotNull
  @NotEmpty
  private final String accountTo;

  @NotNull
  @Min(value = 0, message = "Transfer balance must be positive.")
  private BigDecimal transferAmount;

  @JsonCreator
  public TransferBetweenAccount(@JsonProperty("accountFrom") String accountFrom,
      @JsonProperty("accountTo") String accountTo,
      @JsonProperty("transferAmount") BigDecimal transferAmount) {
    this.accountFrom = accountFrom;
    this.accountTo = accountTo;
    this.transferAmount = transferAmount;
  }
}
