package com.github.phantomtrupe.apigatewayservice.config;

import com.github.phantomtrupe.commons.JWT.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.slf4j.LoggerFactory;
import javax.crypto.SecretKey;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

@Configuration
public class JwtConfig {
    @Bean
    public JwtUtil jwtUtil(@Value("${auth.jwt.secret}") String base64Secret) {
        try {
            return new JwtUtil(base64Secret);
        } catch (Exception e) {
            LoggerFactory.getLogger(JwtConfig.class)
                .warn("Invalid or weak JWT secret provided ({}). Generating a new secure key.", e.getMessage());
            SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            String generatedSecret = Encoders.BASE64.encode(key.getEncoded());
            return new JwtUtil(generatedSecret);
        }
    }
}
