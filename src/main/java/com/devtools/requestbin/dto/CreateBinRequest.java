package com.devtools.requestbin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreateBinRequest {

  @Min(value = 1, message = "Expiry hours must be at least 0")
  @Max(value = 168, message = "Expiry hours cannot exceed 168 hours (7 days)")
  private Integer expiryHours = 24; // default 24 hours

  @Min(value = 10, message = "Max requests must be at least 10")
  @Max(value = 10000, message = "Max requests cannot exceed 10,000")
  private Integer maxRequests = 1000; // default 1000 requests
}