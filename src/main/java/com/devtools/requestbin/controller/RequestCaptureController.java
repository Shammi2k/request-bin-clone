package com.devtools.requestbin.controller;

import java.util.Map;

import com.devtools.requestbin.dto.CapturedRequestResponse;
import com.devtools.requestbin.service.RequestCaptureService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RequestCaptureController
{

  private final RequestCaptureService requestCaptureService;

  // This endpoint captures ALL HTTP methods (GET, POST, PUT, DELETE, PATCH, etc.)
  @RequestMapping(value = "/b/{uniqueUrl}", method = {
    RequestMethod.GET,
    RequestMethod.POST,
    RequestMethod.PUT,
    RequestMethod.DELETE,
    RequestMethod.PATCH,
    RequestMethod.HEAD,
    RequestMethod.OPTIONS
  })
  public ResponseEntity<Map<String, Object>> captureRequest(
    @PathVariable String uniqueUrl,
    HttpServletRequest request)
  {

    CapturedRequestResponse capturedRequest = requestCaptureService.captureRequest(uniqueUrl, request);

    // Return simple response to the sender
    Map<String, Object> response = Map.of(
      "status", "success",
      "message", "Request captured",
      "requestId", capturedRequest.getId(),
      "timestamp", capturedRequest.getTimestamp()
    );

    return ResponseEntity.ok(response);
  }
}