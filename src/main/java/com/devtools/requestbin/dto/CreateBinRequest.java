package com.devtools.requestbin.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreateBinRequest {

  @Min(value = 1, message = "Expiry hours must be at least 1")
  private Integer expiryHours = 24; // default 24 hours

  @Min(value = 10, message = "Max requests must be at least 10")
  private Integer maxRequests = 1000; // default 1000 requests
}