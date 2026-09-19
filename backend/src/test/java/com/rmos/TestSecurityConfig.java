package com.rmos;

import com.rmos.security.JwtAuthFilter;
import com.rmos.security.JwtUtils;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtUtils jwtUtils() {
        return Mockito.mock(JwtUtils.class);
    }
}
