package com.devtools.requestbin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinDetailsResponse {
  private Long id;
  private String uniqueUrl;
  private String fullUrl;
  private LocalDateTime createdAt;
  private LocalDateTime expiresAt;
  private Integer maxRequests;
  private Integer currentRequestCount;
  private List<CapturedRequestResponse> requests; // NEW: list of captured requests
}