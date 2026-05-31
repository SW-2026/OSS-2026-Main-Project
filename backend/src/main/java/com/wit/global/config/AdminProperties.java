package com.wit.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

// 관리자 이메일 화이트리스트 — 콤마 구분 String (env ADMIN_EMAILS override).
// List<String>는 ${} 단일 값으로 자동 split이 안 되므로 String + split로 처리.
// (LoraCatalogProperties/StorageProperties @ConfigurationProperties 패턴 미러)
@Component
@ConfigurationProperties(prefix = "admin")
@Getter
@Setter
public class AdminProperties {

    private String emailWhitelist = "";

    public List<String> emails() {
        if (emailWhitelist == null || emailWhitelist.isBlank()) {
            return List.of();
        }
        return Arrays.stream(emailWhitelist.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    public boolean isAdmin(String email) {
        return email != null && emails().contains(email);
    }
}
