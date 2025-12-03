package com.devtools.requestbin.exception;

import java.time.LocalDateTime;

/**
 * Throws when a bin exists but has passed its expiration time
 */
public class BinExpiredException
  extends RuntimeException
{

  private final LocalDateTime expiredAt;

  public BinExpiredException(String uniqueUrl, LocalDateTime expiredAt)
  {
    super("Bin expired at: " + expiredAt + " (URL: " + uniqueUrl + ")");
    this.expiredAt = expiredAt;
  }

  public LocalDateTime getExpiredAt()
  {
    return expiredAt;
  }
}