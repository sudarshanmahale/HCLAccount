package com.db.awmd.challenge.exception;

public class AccountNotNotFoundException extends RuntimeException{
  public AccountNotNotFoundException(String message) {
    super(message);
  }
}
