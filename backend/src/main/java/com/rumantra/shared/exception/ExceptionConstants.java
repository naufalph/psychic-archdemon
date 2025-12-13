package com.rumantra.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionConstants {
  BID_NOT_DRAFT("BID_NOT_DRAFT", HttpStatus.BAD_REQUEST),
  BID_NOT_FOUND("BID_NOT_FOUND", HttpStatus.NOT_FOUND),
  BID_IMAGE_NOT_FOUND("BID_IMAGE_NOT_FOUND", HttpStatus.NOT_FOUND),
  UNAUTHORIZED_BID_ACCESS("UNAUTHORIZED_BID_ACCESS", HttpStatus.FORBIDDEN),
  ARCHITECT_NOT_FOUND("ARCHITECT_NOT_FOUND", HttpStatus.NOT_FOUND),
  PROJECT_NOT_FOUND("PROJECT_NOT_FOUND", HttpStatus.NOT_FOUND);

  private final String code;
  private final HttpStatus httpStatus;

  ExceptionConstants(String code, HttpStatus httpStatus) {
    this.code = code;
    this.httpStatus = httpStatus;
  }

}
