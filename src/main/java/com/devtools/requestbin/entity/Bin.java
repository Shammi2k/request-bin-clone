package com.devtools.requestbin.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bin
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false, length = 50)
  private String uniqueUrl;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  private Integer maxRequests;

  @Column(nullable = false)
  private Integer currentRequestCount;

  @OneToMany(mappedBy = "bin", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CapturedRequest> requests = new ArrayList<>();

  @PrePersist
  protected void onCreate()
  {
    createdAt = LocalDateTime.now();
    currentRequestCount = 0;
  }
}