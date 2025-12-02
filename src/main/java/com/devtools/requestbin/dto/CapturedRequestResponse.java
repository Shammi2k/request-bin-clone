package com.devtools.requestbin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapturedRequestResponse {
  private Long id;
  private String method;
  private Map<String, String> headers;
  private String body;
  private Map<String, String> queryParams;
  private String ipAddress;
  private LocalDateTime timestamp;
}