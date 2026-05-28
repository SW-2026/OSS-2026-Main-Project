package com.wit.lora.repository;

import com.wit.lora.domain.LoraCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoraCatalogRepository extends JpaRepository<LoraCatalog, Long> {
    Optional<LoraCatalog> findByFileName(String fileName);
}
