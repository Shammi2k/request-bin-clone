package com.devtools.requestbin.controller;

import com.devtools.requestbin.dto.ApiResponse;
import com.devtools.requestbin.dto.BinDetailsResponse;
import com.devtools.requestbin.dto.BinResponse;
import com.devtools.requestbin.dto.CreateBinRequest;
import com.devtools.requestbin.service.BinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
  public ResponseEntity<ApiResponse<BinResponse>> createBin(@Valid @RequestBody CreateBinRequest request)
  {
    BinResponse bin = binService.createBin(request);
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
}