package com.devtools.requestbin.repository;

import java.util.Optional;

import com.devtools.requestbin.entity.Bin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BinRepository
  extends JpaRepository<Bin, Long>
{
  Optional<Bin> findByUniqueUrl(String uniqueUrl);

  boolean existsByUniqueUrl(String uniqueUrl);
}