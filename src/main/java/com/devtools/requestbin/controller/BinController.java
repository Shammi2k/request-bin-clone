package com.devtools.requestbin.controller;

import java.util.List;

import com.devtools.requestbin.dto.ApiResponse;
import com.devtools.requestbin.dto.BinDetailsResponse;
import com.devtools.requestbin.dto.BinResponse;
import com.devtools.requestbin.dto.CapturedRequestResponse;
import com.devtools.requestbin.dto.CreateBinRequest;
import com.devtools.requestbin.service.BinService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bins")
@RequiredArgsConstructor
public class BinController
{

  private final BinService binService;

  @PostMapping
  public ResponseEntity<ApiResponse<BinResponse>> createBin(
    @Valid @RequestBody CreateBinRequest request, HttpServletRequest httpRequest)
  {
    String ipAddress = getClientIpAddress(httpRequest);
    BinResponse bin = binService.createBin(request, ipAddress);
    ApiResponse<BinResponse> response = ApiResponse.created(bin, "Bin created successfully");
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{uniqueUrl}")
  public ResponseEntity<ApiResponse<BinResponse>> getBin(@PathVariable String uniqueUrl)
  {
    BinResponse bin = binService.getBinByUniqueUrl(uniqueUrl);
    ApiResponse<BinResponse> response = ApiResponse.success(bin, "Bin retrieved successfully");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{uniqueUrl}/details")
  public ResponseEntity<ApiResponse<BinDetailsResponse>> getBinDetails(@PathVariable String uniqueUrl)
  {
    BinDetailsResponse details = binService.getBinDetailsWithRequests(uniqueUrl);
    ApiResponse<BinDetailsResponse> response = ApiResponse.success(details, "Bin details retrieved successfully");
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{uniqueUrl}")
  public ResponseEntity<ApiResponse<Void>> deleteBin(@PathVariable String uniqueUrl)
  {
    binService.deleteBin(uniqueUrl);
    ApiResponse<Void> response = ApiResponse.success(null, "Bin deleted successfully");
    return ResponseEntity.ok(response);
  }

  /**
   * Export bin requests as JSON
   * <p>
   * HttpHeaders - Used to set Content-Disposition for file download
   * MediaType.APPLICATION_JSON - Sets response content type
   */
  @GetMapping("/{uniqueUrl}/export/json")
  public ResponseEntity<List<CapturedRequestResponse>> exportJson(
    @PathVariable String uniqueUrl)
  {

    List<CapturedRequestResponse> requests = binService.getRequestsForExport(uniqueUrl);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setContentDisposition(
      ContentDisposition.attachment()
        .filename(uniqueUrl + "_requests.json")
        .build()
    );

    return ResponseEntity.ok()
      .headers(headers)
      .body(requests);
  }

  /**
   * Export bin requests as CSV
   */
  @GetMapping("/{uniqueUrl}/export/csv")
  public ResponseEntity<String> exportCsv(
    @PathVariable String uniqueUrl)
  {

    String csv = binService.exportRequestsAsCsv(uniqueUrl);

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
    headers.setContentDisposition(
      ContentDisposition.attachment()
        .filename(uniqueUrl + "_requests.csv")
        .build()
    );

    return ResponseEntity.ok()
      .headers(headers)
      .body(csv);
  }

  /**
   * Extract client IP address from request
   * Handles cases where request is behind proxy/load balancer
   */
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
    // If multiple IPs (comma-separated), take the first one
    if (ip != null && ip.contains(","))
    {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }
}