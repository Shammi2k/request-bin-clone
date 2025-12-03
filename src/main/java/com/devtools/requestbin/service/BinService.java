package com.devtools.requestbin.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.devtools.requestbin.dto.BinDetailsResponse;
import com.devtools.requestbin.dto.BinResponse;
import com.devtools.requestbin.dto.CapturedRequestResponse;
import com.devtools.requestbin.dto.CreateBinRequest;
import com.devtools.requestbin.entity.Bin;
import com.devtools.requestbin.entity.CapturedRequest;
import com.devtools.requestbin.exception.BinExpiredException;
import com.devtools.requestbin.exception.BinNotFoundException;
import com.devtools.requestbin.exception.RateLimitExceededException;
import com.devtools.requestbin.repository.BinRepository;
import com.devtools.requestbin.repository.CapturedRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinService
{

  private final BinRepository binRepository;
  private final CapturedRequestRepository capturedRequestRepository;
  private final RateLimitService rateLimitService;

  @Value("${server.port:8080}")
  private String serverPort;

  @Transactional
  public BinResponse createBin(CreateBinRequest request, String ipAddress)
  {  // ADD ipAddress parameter
    // Check rate limit
    if (!rateLimitService.allowBinCreation(ipAddress))
    {
      throw new RateLimitExceededException("Rate limit exceeded. You can only create 10 bins per hour. Please try again later.");
    }

    String uniqueUrl = generateUniqueUrl();

    Bin bin = Bin.builder()
      .uniqueUrl(uniqueUrl)
      .expiresAt(LocalDateTime.now().plusHours(request.getExpiryHours()))
      .maxRequests(request.getMaxRequests())
      .currentRequestCount(0)
      .build();

    Bin savedBin = binRepository.save(bin);
    log.info("Created new bin with uniqueUrl: {} from IP: {}", uniqueUrl, ipAddress);

    return mapToResponse(savedBin);
  }

  @Transactional(readOnly = true)
  public BinResponse getBinByUniqueUrl(String uniqueUrl)
  {
    Bin bin = binRepository.findByUniqueUrl(uniqueUrl)
      .orElseThrow(() -> new BinNotFoundException(uniqueUrl));

    // Check if expired
    if (bin.getExpiresAt().isBefore(LocalDateTime.now()))
    {
      throw new BinExpiredException(uniqueUrl, bin.getExpiresAt());
    }

    return mapToResponse(bin);
  }

  @Transactional(readOnly = true)
  public BinDetailsResponse getBinDetailsWithRequests(String uniqueUrl)
  {
    Bin bin = binRepository.findByUniqueUrl(uniqueUrl)
      .orElseThrow(() -> new BinNotFoundException(uniqueUrl));

    if (bin.getExpiresAt().isBefore(LocalDateTime.now()))
    {
      throw new BinExpiredException(uniqueUrl, bin.getExpiresAt());
    }

    // Get all requests for this bin
    List<CapturedRequest> requests = capturedRequestRepository.findByBinIdOrderByTimestampDesc(bin.getId());

    // Map to response
    List<CapturedRequestResponse> requestResponses = requests.stream()
      .map(this::mapRequestToResponse)
      .collect(Collectors.toList());

    String fullUrl = String.format("http://localhost:%s/b/%s", serverPort, bin.getUniqueUrl());

    return BinDetailsResponse.builder()
      .id(bin.getId())
      .uniqueUrl(bin.getUniqueUrl())
      .fullUrl(fullUrl)
      .createdAt(bin.getCreatedAt())
      .expiresAt(bin.getExpiresAt())
      .maxRequests(bin.getMaxRequests())
      .currentRequestCount(bin.getCurrentRequestCount())
      .requests(requestResponses)
      .build();
  }

  private CapturedRequestResponse mapRequestToResponse(CapturedRequest request)
  {
    // Parse JSON strings back to maps
    Map<String, String> headers = parseJson(request.getHeaders());
    Map<String, String> queryParams = parseJson(request.getQueryParams());

    return CapturedRequestResponse.builder()
      .id(request.getId())
      .method(request.getMethod())
      .headers(headers)
      .body(request.getBody())
      .queryParams(queryParams)
      .ipAddress(request.getIpAddress())
      .timestamp(request.getTimestamp())
      .build();
  }

  private Map<String, String> parseJson(String json)
  {
    try
    {
      return new ObjectMapper().readValue(json, Map.class);
    }
    catch (Exception e)
    {
      return new HashMap<>();
    }
  }

  @Transactional
  public void deleteBin(String uniqueUrl)
  {
    Bin bin = binRepository.findByUniqueUrl(uniqueUrl)
      .orElseThrow(() -> new BinNotFoundException(uniqueUrl));

    binRepository.delete(bin);
    log.info("Deleted bin with uniqueUrl: {}", uniqueUrl);
  }

  private String generateUniqueUrl()
  {
    String uniqueUrl;
    do
    {
      // Generate 8-character unique code
      uniqueUrl = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
    while (binRepository.existsByUniqueUrl(uniqueUrl));

    return uniqueUrl;
  }

  private BinResponse mapToResponse(Bin bin)
  {
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