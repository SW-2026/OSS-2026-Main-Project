package com.wit.member.repository;

import com.wit.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 1. 이메일로 회원 정보 찾기 (로그인 등에서 사용)
    // 결과가 없을 수도 있으므로 Optional로 감싸는 것이 안전
    Optional<Member> findByEmail(String email);

    // 2. 이메일 존재 여부 확인 (회원가입 중복 체크 등에서 사용)
    boolean existsByEmail(String email);
}
