package com.devtools.requestbin.controller;

import com.devtools.requestbin.dto.BinDetailsResponse;
import com.devtools.requestbin.dto.BinResponse;
import com.devtools.requestbin.dto.CreateBinRequest;
import com.devtools.requestbin.service.BinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bins")
@RequiredArgsConstructor
public class BinController {

  private final BinService binService;

  @PostMapping
  public ResponseEntity<BinResponse> createBin(@Valid @RequestBody CreateBinRequest request) {
    BinResponse response = binService.createBin(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{uniqueUrl}")
  public ResponseEntity<BinResponse> getBin(@PathVariable String uniqueUrl) {
    BinResponse response = binService.getBinByUniqueUrl(uniqueUrl);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{uniqueUrl}/details")
  public ResponseEntity<BinDetailsResponse> getBinDetails(@PathVariable String uniqueUrl) {
    BinDetailsResponse response = binService.getBinDetailsWithRequests(uniqueUrl);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{uniqueUrl}")
  public ResponseEntity<Void> deleteBin(@PathVariable String uniqueUrl) {
    binService.deleteBin(uniqueUrl);
    return ResponseEntity.noContent().build();
  }
}