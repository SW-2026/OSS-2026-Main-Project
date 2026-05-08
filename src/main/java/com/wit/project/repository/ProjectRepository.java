package com.wit.project.repository;

import com.wit.project.domain.Project; // Project 엔티티 경로 확인
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // JpaRepository를 상속받는 것만으로 findById, save, findAll 등이 자동 생성됩니다.
}