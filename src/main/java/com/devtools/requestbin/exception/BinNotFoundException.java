package com.devtools.requestbin.exception;

/**
 * Throws when a bin with given uniqueUrl doesn't exist in database
 * <p>
 * Annotations:
 * - No annotations needed for exception classes
 * - They extend RuntimeException (unchecked exception)
 */
public class BinNotFoundException
  extends RuntimeException
{

  public BinNotFoundException(String uniqueUrl)
  {
    super("Bin not found with URL: " + uniqueUrl);
  }
}