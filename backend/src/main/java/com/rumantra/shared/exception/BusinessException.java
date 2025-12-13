package com.rumantra.shared.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
  private final ExceptionConstants exceptionCode;

  public BusinessException(ExceptionConstants exceptionCode) {
    super(exceptionCode.getCode());
    this.exceptionCode = exceptionCode;
  }

}
