package com.github.phantomtrupe.apigatewayservice.config;

import com.github.phantomtrupe.commons.JWT.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Bean
    public JwtUtil jwtUtil(@Value("${auth.jwt.secret}") String secret) {
        return new JwtUtil(secret);
    }
}
