package com.devtools.requestbin.exception;

/**
 * Throws when a bin has received maximum allowed requests
 */
public class BinLimitExceededException
  extends RuntimeException
{

  private final int maxRequests;
  private final int currentCount;

  public BinLimitExceededException(String uniqueUrl, int maxRequests, int currentCount)
  {
    super(String.format(
      "Bin has reached maximum request limit. Max: %d, Current: %d (URL: %s)",
      maxRequests, currentCount, uniqueUrl));
    this.maxRequests = maxRequests;
    this.currentCount = currentCount;
  }

  public int getMaxRequests()
  {
    return maxRequests;
  }

  public int getCurrentCount()
  {
    return currentCount;
  }
}