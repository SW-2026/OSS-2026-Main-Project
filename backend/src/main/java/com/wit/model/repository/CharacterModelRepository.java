package com.wit.model.repository;

import com.wit.model.domain.CharacterModel;
import com.wit.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CharacterModelRepository extends JpaRepository<CharacterModel, Long> {

    List<CharacterModel> findAllByProject(Project project);

    // LoRA 자동 등록 멱등성 — 같은 프로젝트에 같은 LoRA 이미 등록됐는지 확인
    Optional<CharacterModel> findByProjectAndLoraModelPath(Project project, String loraModelPath);
}
