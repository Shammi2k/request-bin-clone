package com.devtools.requestbin.repository;

import java.util.List;

import com.devtools.requestbin.entity.CapturedRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CapturedRequestRepository
  extends JpaRepository<CapturedRequest, Long>
{
  List<CapturedRequest> findByBinIdOrderByTimestampDesc(Long binId);

  long countByBinId(Long binId);
}