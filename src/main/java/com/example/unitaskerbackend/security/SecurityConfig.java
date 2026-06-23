package com.example.unitaskerbackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // SADECE giriş, kayıt ve HTML arayüzüne biletsiz izin veriyoruz
                        .requestMatchers(
                                "/auth/**",
                                "/",
                                "/index.html",
                                "/css/**",
                                "/js/**",
                                "/uploads/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/ws/**"               // YENİ: Canlı Bildirim (WebSocket) Kapısı
                        ).permitAll()                        .anyRequest().authenticated()
                )
                // Kendi yazdığımız güvenlik görevlisini kapıya dikiyoruz
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}