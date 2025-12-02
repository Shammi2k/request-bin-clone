package com.devtools.requestbin.service;

import com.devtools.requestbin.dto.BinResponse;
import com.devtools.requestbin.dto.CreateBinRequest;
import com.devtools.requestbin.entity.Bin;
import com.devtools.requestbin.repository.BinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinService {

  private final BinRepository binRepository;

  @Value("${server.port:8080}")
  private String serverPort;

  @Transactional
  public BinResponse createBin(CreateBinRequest request) {
    String uniqueUrl = generateUniqueUrl();

    Bin bin = Bin.builder()
      .uniqueUrl(uniqueUrl)
      .expiresAt(LocalDateTime.now().plusHours(request.getExpiryHours()))
      .maxRequests(request.getMaxRequests())
      .currentRequestCount(0)
      .build();

    Bin savedBin = binRepository.save(bin);
    log.info("Created new bin with uniqueUrl: {}", uniqueUrl);

    return mapToResponse(savedBin);
  }

  @Transactional(readOnly = true)
  public BinResponse getBinByUniqueUrl(String uniqueUrl) {
    Bin bin = binRepository.findByUniqueUrl(uniqueUrl)
      .orElseThrow(() -> new RuntimeException("Bin not found: " + uniqueUrl));

    // Check if expired
    if (bin.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new RuntimeException("Bin has expired");
    }

    return mapToResponse(bin);
  }

  @Transactional
  public void deleteBin(String uniqueUrl) {
    Bin bin = binRepository.findByUniqueUrl(uniqueUrl)
      .orElseThrow(() -> new RuntimeException("Bin not found: " + uniqueUrl));

    binRepository.delete(bin);
    log.info("Deleted bin with uniqueUrl: {}", uniqueUrl);
  }

  private String generateUniqueUrl() {
    String uniqueUrl;
    do {
      // Generate 8-character unique code
      uniqueUrl = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    } while (binRepository.existsByUniqueUrl(uniqueUrl));

    return uniqueUrl;
  }

  private BinResponse mapToResponse(Bin bin) {
    String fullUrl = String.format("http://localhost:%s/b/%s", serverPort, bin.getUniqueUrl());

    return BinResponse.builder()
      .id(bin.getId())
      .uniqueUrl(bin.getUniqueUrl())
      .fullUrl(fullUrl)
      .createdAt(bin.getCreatedAt())
      .expiresAt(bin.getExpiresAt())
      .maxRequests(bin.getMaxRequests())
      .currentRequestCount(bin.getCurrentRequestCount())
      .build();
  }
}