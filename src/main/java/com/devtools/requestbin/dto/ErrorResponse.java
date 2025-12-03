package com.devtools.requestbin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response structure sent to clients
 * <p>
 * Used by GlobalExceptionHandler to format all errors consistently
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse
{

  private LocalDateTime timestamp;      // When error occurred
  private int status;                   // HTTP status code (404, 400, 500, etc.)
  private String error;                 // HTTP status text ("Not Found", "Bad Request")
  private String message;               // Human-readable error message
  private String path;                  // Which endpoint caused the error

  /**
   * Factory method for quick creation
   */
  public static ErrorResponse of(int status, String error, String message, String path)
  {
    return ErrorResponse.builder()
      .timestamp(LocalDateTime.now())
      .status(status)
      .error(error)
      .message(message)
      .path(path)
      .build();
  }
}