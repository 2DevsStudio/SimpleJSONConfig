package com.twodevsstudio.simplejsonconfig.exceptions;

public class AnnotationProcessException extends RuntimeException {

  public AnnotationProcessException() {
    super("Annotations are already processed.");
  }

  public AnnotationProcessException(String message) {
    super(message);
  }

  public AnnotationProcessException(String message, Throwable cause) {
    super(message, cause);
  }

  public AnnotationProcessException(Throwable cause) {
    super(cause);
  }

  public AnnotationProcessException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
