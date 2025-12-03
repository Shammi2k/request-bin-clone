package com.devtools.requestbin.service;

import com.devtools.requestbin.entity.Bin;
import com.devtools.requestbin.repository.BinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for scheduled background tasks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksService
{

  private final BinRepository binRepository;

  /**
   * Deletes expired bins every hour
   * <p>
   * fixedRate = 3600000 milliseconds = 60 minutes = 1 hour
   * initialDelay = 60000 milliseconds = 1 minute (wait before first run)
   */
  @Scheduled(fixedRate = 3600000, initialDelay = 60000)
  @Transactional
  public void deleteExpiredBins()
  {
    log.info("Running scheduled task: Delete expired bins");

    LocalDateTime now = LocalDateTime.now();

    // Find all bins that have expired
    List<Bin> expiredBins = binRepository.findAll().stream()
      .filter(bin -> bin.getExpiresAt().isBefore(now))
      .toList();

    if (expiredBins.isEmpty())
    {
      log.info("No expired bins found");
      return;
    }

    // Delete them
    binRepository.deleteAll(expiredBins);

    log.info("Deleted {} expired bins", expiredBins.size());
  }

  /**
   * Logs statistics every 30 minutes (for monitoring)
   */

  @Scheduled(fixedRate = 1800000, initialDelay = 60000)

  public void logStatistics()
  {
    long totalBins = binRepository.count();
    LocalDateTime now = LocalDateTime.now();
    long activeBins = binRepository.findAll().stream()
      .filter(bin -> bin.getExpiresAt().isAfter(now))
      .count();
    log.info(
      "Statistics - Total bins: {}, Active bins: {}, Expired: {}",
      totalBins, activeBins, totalBins - activeBins);
  }
}
