package com.devtools.requestbin.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting service using token bucket algorithm
 * <p>
 * Token Bucket Algorithm:
 * - Each user/IP gets a "bucket" with tokens
 * - Each action consumes 1 token
 * - Tokens refill at a fixed rate
 * - If bucket is empty, request is denied
 */
@Service
public class RateLimitService
{

  // Store buckets for each IP address
  private final Map<String, Bucket> cacheBinCreation = new ConcurrentHashMap<>();
  private final Map<String, Bucket> cacheRequestCapture = new ConcurrentHashMap<>();

  /**
   * Check if IP can create a bin
   * Limit: 10 bins per hour per IP
   */
  public boolean allowBinCreation(String ipAddress)
  {
    Bucket bucket = cacheBinCreation.computeIfAbsent(ipAddress, k -> createBinCreationBucket());
    return bucket.tryConsume(1);
  }

  /**
   * Check if bin can accept more requests
   * Limit: 60 requests per minute per bin
   */
  public boolean allowRequestCapture(String binId)
  {
    Bucket bucket = cacheRequestCapture.computeIfAbsent(binId, k -> createRequestCaptureBucket());
    return bucket.tryConsume(1);
  }

  private Bucket createBinCreationBucket()
  {
    // Allow 10 requests per hour
    Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofHours(1)));
    return Bucket.builder()
      .addLimit(limit)
      .build();
  }

  private Bucket createRequestCaptureBucket()
  {
    // Allow 60 requests per minute
    Bandwidth limit = Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1)));
    return Bucket.builder()
      .addLimit(limit)
      .build();
  }
}