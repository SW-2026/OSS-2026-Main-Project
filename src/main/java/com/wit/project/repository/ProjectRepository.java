package com.wit.project.repository;

import com.wit.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // JpaRepository를 상속받는 것만으로 findById, save, findAll 등이 자동 생성됩니다.

    // 특정 회원이 소유한 프로젝트 목록을 조회 (최신 생성 순 정렬)
    List<Project> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId);
}
