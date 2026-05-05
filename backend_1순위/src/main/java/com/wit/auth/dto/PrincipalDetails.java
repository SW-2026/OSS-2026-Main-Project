package com.wit.auth.dto;

import com.wit.member.domain.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

// Member 엔티티와 결합, 인증 객체
@Getter
public class PrincipalDetails implements UserDetails {

    private final Member member; // Member 엔티티

    public PrincipalDetails(Member member) {
        this.member = member;
    }

    // 권한 관련 설정 (현재는 빈 리스트나 기본 권한 부여)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(() -> "ROLE_USER"); // 기본 권한 설정
        return collect;
    }

    @Override
    public String getPassword() {
        return member.getPassword(); // Member 엔티티의 비밀번호
    }

    @Override
    public String getUsername() {
        return member.getEmail(); // 로그인에 사용할 식별값 (이메일)
    }

    // 계정 상태 체크 (필요에 따라 로직 추가 가능, 기본은 true)
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}