package com.devtools.requestbin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "captured_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapturedRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bin_id", nullable = false)
  private Bin bin;

  @Column(nullable = false, length = 10)
  private String method; // GET, POST, PUT, DELETE, etc.

  @Column(columnDefinition = "TEXT")
  private String headers;

  @Column(columnDefinition = "TEXT")
  private String body;

  @Column(columnDefinition = "TEXT")
  private String queryParams;

  @Column(length = 45)
  private String ipAddress;

  @Column(nullable = false)
  private LocalDateTime timestamp;

  @PrePersist
  protected void onCreate() {
    timestamp = LocalDateTime.now();
  }
}