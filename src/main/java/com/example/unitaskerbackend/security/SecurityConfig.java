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
                        // 1. HERKESE AÇIK ALANLAR (Biletsiz giriş serbest)
                        .requestMatchers(
                                "/auth/**",
                                "/",
                                "/index.html",
                                "/css/**",
                                "/js/**",
                                "/uploads/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/ws/**"               // Canlı Bildirim (WebSocket) Kapısı
                        ).permitAll()

                        // 2. 🚨 GÜVENLİK YAMASI: ADMIN ARKA KAPISI
                        // Bu adrese sadece veritabanında rolü ROLE_ADMIN olanlar istek atabilir!
                        // (HasRole metodu arka planda otomatik olarak "ROLE_" prefix'ini ekler,
                        // bu yüzden sadece "ADMIN" yazmamız yeterlidir.)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 3. GERİ KALAN HER ŞEY (Görev ekleme, profil resmi vb.)
                        // Sadece sisteme giriş yapmış (Token'ı olan) herkes yapabilir.
                        .anyRequest().authenticated()
                )
                // Kendi yazdığımız güvenlik görevlisini kapıya dikiyoruz
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}