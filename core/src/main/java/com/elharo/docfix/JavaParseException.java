package com.elharo.docfix;

/**
 * Exception thrown when there is an error parsing Java source code.
 */
public class JavaParseException extends Exception {
  
  /**
   * Constructs a new JavaParseException with the specified detail message.
   *
   * @param message the detail message
   */
  public JavaParseException(String message) {
    super(message);
  }
  
  /**
   * Constructs a new JavaParseException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of this exception
   */
  public JavaParseException(String message, Throwable cause) {
    super(message, cause);
  }
}