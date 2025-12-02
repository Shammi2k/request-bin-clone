package com.devtools.requestbin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinResponse {
  private Long id;
  private String uniqueUrl;
  private String fullUrl; // e.g., http://localhost:8080/b/{uniqueUrl}
  private LocalDateTime createdAt;
  private LocalDateTime expiresAt;
  private Integer maxRequests;
  private Integer currentRequestCount;
}