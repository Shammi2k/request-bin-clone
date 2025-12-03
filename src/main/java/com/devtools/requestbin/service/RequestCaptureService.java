package com.devtools.requestbin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.devtools.requestbin.dto.CapturedRequestResponse;
import com.devtools.requestbin.dto.ReplayRequest;
import com.devtools.requestbin.entity.Bin;
import com.devtools.requestbin.entity.CapturedRequest;
import com.devtools.requestbin.exception.BinExpiredException;
import com.devtools.requestbin.exception.BinLimitExceededException;
import com.devtools.requestbin.exception.BinNotFoundException;
import com.devtools.requestbin.exception.RateLimitExceededException;
import com.devtools.requestbin.repository.BinRepository;
import com.devtools.requestbin.repository.CapturedRequestRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestCaptureService
{

  private final BinRepository binRepository;
  private final CapturedRequestRepository requestRepository;
  private final RateLimitService rateLimitService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Transactional
  public CapturedRequestResponse captureRequest(String uniqueUrl, HttpServletRequest request) {
    // 1. Find the bin
    Bin bin = binRepository.findByUniqueUrl(uniqueUrl)
      .orElseThrow(() -> new BinNotFoundException(uniqueUrl));

    // 2. Check if bin is expired
    if (bin.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new BinExpiredException(uniqueUrl, bin.getExpiresAt());
    }

    // 3. Check rate limit (60 requests per minute per bin)
    if (!rateLimitService.allowRequestCapture(bin.getId().toString())) {
      throw new RateLimitExceededException(
        "Rate limit exceeded for this bin. Maximum 60 requests per minute allowed."
      );
    }

    // 4. Check if bin has reached max requests
    if (bin.getCurrentRequestCount() >= bin.getMaxRequests()) {
      throw new BinLimitExceededException(uniqueUrl, bin.getMaxRequests(), bin.getCurrentRequestCount());
    }

    // 4. Extract request details
    String method = request.getMethod();
    String headers = extractHeaders(request);
    String body = extractBody(request);
    String queryParams = extractQueryParams(request);
    String ipAddress = getClientIpAddress(request);

    // 5. Create and save captured request
    CapturedRequest capturedRequest = CapturedRequest.builder()
      .bin(bin)
      .method(method)
      .headers(headers)
      .body(body)
      .queryParams(queryParams)
      .ipAddress(ipAddress)
      .build();

    CapturedRequest saved = requestRepository.save(capturedRequest);

    // 6. Increment bin request count
    bin.setCurrentRequestCount(bin.getCurrentRequestCount() + 1);
    binRepository.save(bin);

    log.info("Captured {} request for bin: {} from IP: {}", method, uniqueUrl, ipAddress);

    return mapToResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<CapturedRequestResponse> getRequestsForBin(String uniqueUrl)
  {
    Bin bin = binRepository.findByUniqueUrl(uniqueUrl)
      .orElseThrow(() -> new BinNotFoundException(uniqueUrl));

    List<CapturedRequest> requests = requestRepository.findByBinIdOrderByTimestampDesc(bin.getId());

    return requests.stream()
      .map(this::mapToResponse)
      .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public Map<String, Object> replayRequest(Long requestId, ReplayRequest replayRequest) {
    CapturedRequest capturedRequest = requestRepository.findById(requestId)
      .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));

    try {
      // Parse headers
      Map<String, String> headers = parseJsonToMap(capturedRequest.getHeaders());

      // Add or override headers if provided
      if (replayRequest.getAdditionalHeaders() != null) {
        headers.putAll(replayRequest.getAdditionalHeaders());
      }

      // Use override body if provided, otherwise use original
      String body = replayRequest.getOverrideBody() != null ?
        replayRequest.getOverrideBody() : capturedRequest.getBody();

      // Make HTTP request using RestTemplate
      RestTemplate restTemplate = new RestTemplate();

      HttpHeaders httpHeaders = new HttpHeaders();
      headers.forEach(httpHeaders::add);

      HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);

      ResponseEntity<String> response;

      // Call based on original method
      switch (capturedRequest.getMethod()) {
        case "POST":
          response = restTemplate.postForEntity(replayRequest.getTargetUrl(), entity, String.class);
          break;
        case "PUT":
          response = restTemplate.exchange(replayRequest.getTargetUrl(), HttpMethod.PUT, entity, String.class);
          break;
        case "DELETE":
          response = restTemplate.exchange(replayRequest.getTargetUrl(), HttpMethod.DELETE, entity, String.class);
          break;
        case "PATCH":
          response = restTemplate.exchange(replayRequest.getTargetUrl(), HttpMethod.PATCH, entity, String.class);
          break;
        case "GET":
        default:
          response = restTemplate.exchange(replayRequest.getTargetUrl(), HttpMethod.GET, entity, String.class);
          break;
      }

      // Return result
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("statusCode", response.getStatusCode().value());
      result.put("responseBody", response.getBody());
      result.put("responseHeaders", response.getHeaders());
      result.put("originalRequestId", requestId);
      result.put("targetUrl", replayRequest.getTargetUrl());

      log.info("Replayed request {} to {}", requestId, replayRequest.getTargetUrl());

      return result;

    } catch (Exception e) {
      log.error("Error replaying request: {}", e.getMessage());

      Map<String, Object> result = new HashMap<>();
      result.put("success", false);
      result.put("error", e.getMessage());
      result.put("originalRequestId", requestId);
      result.put("targetUrl", replayRequest.getTargetUrl());

      return result;
    }
  }

  private String extractHeaders(HttpServletRequest request)
  {
    Map<String, String> headersMap = new HashMap<>();
    Enumeration<String> headerNames = request.getHeaderNames();

    while (headerNames.hasMoreElements())
    {
      String headerName = headerNames.nextElement();
      String headerValue = request.getHeader(headerName);
      headersMap.put(headerName, headerValue);
    }

    try
    {
      return objectMapper.writeValueAsString(headersMap);
    }
    catch (JsonProcessingException e)
    {
      log.error("Error serializing headers", e);
      return "{}";
    }
  }

  private String extractBody(HttpServletRequest request)
  {
    try
    {
      BufferedReader reader = request.getReader();
      return reader.lines().collect(Collectors.joining("\n"));
    }
    catch (IOException e)
    {
      log.error("Error reading request body", e);
      return "";
    }
  }

  private String extractQueryParams(HttpServletRequest request)
  {
    Map<String, String> paramsMap = new HashMap<>();
    Map<String, String[]> parameterMap = request.getParameterMap();

    for (Map.Entry<String, String[]> entry : parameterMap.entrySet())
    {
      String key = entry.getKey();
      String[] values = entry.getValue();
      paramsMap.put(key, values.length > 0 ? values[0] : "");
    }

    try
    {
      return objectMapper.writeValueAsString(paramsMap);
    }
    catch (JsonProcessingException e)
    {
      log.error("Error serializing query params", e);
      return "{}";
    }
  }

  private String getClientIpAddress(HttpServletRequest request)
  {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
    {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
    {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
    {
      ip = request.getRemoteAddr();
    }
    return ip;
  }

  private CapturedRequestResponse mapToResponse(CapturedRequest request)
  {
    Map<String, String> headersMap = parseJsonToMap(request.getHeaders());
    Map<String, String> queryParamsMap = parseJsonToMap(request.getQueryParams());

    return CapturedRequestResponse.builder()
      .id(request.getId())
      .method(request.getMethod())
      .headers(headersMap)
      .body(request.getBody())
      .queryParams(queryParamsMap)
      .ipAddress(request.getIpAddress())
      .timestamp(request.getTimestamp())
      .build();
  }

  private Map<String, String> parseJsonToMap(String json)
  {
    try
    {
      return objectMapper.readValue(json, Map.class);
    }
    catch (JsonProcessingException e)
    {
      log.error("Error parsing JSON to map", e);
      return new HashMap<>();
    }
  }
}