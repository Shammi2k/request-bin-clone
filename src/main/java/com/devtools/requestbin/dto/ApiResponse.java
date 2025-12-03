package com.devtools.requestbin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard success response wrapper
 * <p>
 * Generic type <T> allows this to wrap any data type:
 * - ApiResponse<BinResponse>
 * - ApiResponse<List<CapturedRequestResponse>>
 * - ApiResponse<String>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T>
{

  private LocalDateTime timestamp;
  private int status;
  private String message;
  private T data;

  public static <T> ApiResponse<T> success(T data, String message)
  {
    return ApiResponse.<T> builder()
      .timestamp(LocalDateTime.now())
      .status(200)
      .message(message)
      .data(data)
      .build();
  }

  public static <T> ApiResponse<T> created(T data, String message)
  {
    return ApiResponse.<T> builder()
      .timestamp(LocalDateTime.now())
      .status(201)
      .message(message)
      .data(data)
      .build();
  }
}