package com.wit.model.repository;

import com.wit.model.domain.CharacterModel;
import com.wit.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterModelRepository extends JpaRepository<CharacterModel, Long> {

    List<CharacterModel> findAllByProject(Project project);
}
