package com.wit.ai.repository;

import com.wit.ai.domain.AiTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiTaskRepository extends JpaRepository<AiTask, Long> {
}
