package com.devtools.requestbin.exception;

import java.util.HashMap;
import java.util.Map;

import com.devtools.requestbin.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{

  /**
   * Handles BinNotFoundException
   * Returns 404 Not Found
   */
  @ExceptionHandler(BinNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleBinNotFound(
    BinNotFoundException ex,
    HttpServletRequest request)
  {

    log.error("Bin not found: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
      HttpStatus.NOT_FOUND.value(),
      HttpStatus.NOT_FOUND.getReasonPhrase(),
      ex.getMessage(),
      request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Handles BinExpiredException
   * Returns 410 Gone (resource existed but no longer available)
   */
  @ExceptionHandler(BinExpiredException.class)
  public ResponseEntity<ErrorResponse> handleBinExpired(
    BinExpiredException ex,
    HttpServletRequest request)
  {

    log.warn("Bin expired: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
      HttpStatus.GONE.value(),
      HttpStatus.GONE.getReasonPhrase(),
      ex.getMessage(),
      request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.GONE).body(error);
  }

  /**
   * Handles BinLimitExceededException
   * Returns 429 Too Many Requests
   */
  @ExceptionHandler(BinLimitExceededException.class)
  public ResponseEntity<ErrorResponse> handleBinLimitExceeded(
    BinLimitExceededException ex,
    HttpServletRequest request)
  {

    log.warn("Bin limit exceeded: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
      HttpStatus.TOO_MANY_REQUESTS.value(),
      HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
      ex.getMessage(),
      request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
  }

  /**
   * Handles InvalidRequestException
   * Returns 400 Bad Request
   */
  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRequest(
    InvalidRequestException ex,
    HttpServletRequest request)
  {

    log.error("Invalid request: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
      HttpStatus.BAD_REQUEST.value(),
      HttpStatus.BAD_REQUEST.getReasonPhrase(),
      ex.getMessage(),
      request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles validation errors (when @Valid fails on @RequestBody)
   * Returns 400 Bad Request with field-specific error messages
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationErrors(
    MethodArgumentNotValidException ex,
    HttpServletRequest request)
  {

    Map<String, String> fieldErrors = new HashMap<>();

    // Extract field-specific validation errors
    ex.getBindingResult().getAllErrors().forEach(error ->
    {
      String fieldName = ((FieldError)error).getField();
      String errorMessage = error.getDefaultMessage();
      fieldErrors.put(fieldName, errorMessage);
    });

    log.error("Validation failed: {}", fieldErrors);

    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", java.time.LocalDateTime.now());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Validation Failed");
    response.put("message", "Invalid input parameters");
    response.put("fieldErrors", fieldErrors);
    response.put("path", request.getRequestURI());

    return ResponseEntity.badRequest().body(response);
  }

  /**
   * Catches all other exceptions not handled above
   * Returns 500 Internal Server Error
   * <p>
   * This is your safety net - prevents stack traces from being exposed to clients
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
    Exception ex,
    HttpServletRequest request)
  {

    log.error("Unexpected error occurred", ex);  // Log full stack trace

    ErrorResponse error = ErrorResponse.of(
      HttpStatus.INTERNAL_SERVER_ERROR.value(),
      HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
      "An unexpected error occurred. Please try again later.",  // Don't expose internal details
      request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}