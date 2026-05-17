package com.wit.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4.0 Jackson 함정 대응.
 * Spring Framework 7.0이 HttpMessageConverter에 Jackson 3.0을 채택했으나,
 * 본 코드가 Jackson 2.x (com.fasterxml.jackson.databind.ObjectMapper)에 의존.
 * Spring Boot 4.0 자동 구성이 Jackson 2.x ObjectMapper 빈을 등록하지 않으므로
 * 명시적으로 등록.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
