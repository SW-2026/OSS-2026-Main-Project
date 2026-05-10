package com.wit.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// TokenResponse
@Getter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String tokenType = "Bearer";
}