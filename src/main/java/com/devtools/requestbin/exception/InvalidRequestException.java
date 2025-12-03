package com.devtools.requestbin.exception;

/**
 * Throws when client sends invalid data (bad input)
 */
public class InvalidRequestException
  extends RuntimeException
{

  public InvalidRequestException(String message)
  {
    super(message);
  }
}