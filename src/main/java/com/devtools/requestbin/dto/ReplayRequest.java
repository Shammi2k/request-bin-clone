package com.devtools.requestbin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * DTO for replaying captured requests
 */
@Data
public class ReplayRequest
{

  @NotBlank(message = "Target URL is required")
  private String targetUrl;

  // Optional: Override headers
  private Map<String, String> additionalHeaders;

  // Optional: Override body
  private String overrideBody;
}