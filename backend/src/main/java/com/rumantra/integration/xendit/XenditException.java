package com.rumantra.integration.xendit;

public class XenditException extends RuntimeException {
  public XenditException(String message) {
    super(message);
  }

  public XenditException(String message, Throwable cause) {
    super(message, cause);
  }
}
