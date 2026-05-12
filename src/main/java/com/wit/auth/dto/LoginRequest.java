package com.wit.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// LoginRequest
@Getter
@NoArgsConstructor
public class LoginRequest {
    @NotBlank private String email;
    @NotBlank private String password;
}
