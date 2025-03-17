package com.chat.kit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                                .requestMatchers("/ws/**").permitAll()
                                .requestMatchers("/**").permitAll()
                                .requestMatchers("/wss/**").permitAll()
//                        .anyRequest().authenticated()
                                .requestMatchers("/h2-console/**").permitAll()
                                .requestMatchers("/members/**").permitAll()
                                .requestMatchers("/chat/**").permitAll()
                                .anyRequest().permitAll() // 모든 요청을 허용

                )
                .cors().disable()
                .csrf().disable() // CSRF 비활성화 (Postman을 통한 테스트 시 편의성 제공)
                .headers(headers -> headers.frameOptions().sameOrigin()) // h2-consoel
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt()); // OAuth2 JWT 인증 설정

        return http.build();
    }





    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return urlBasedCorsConfigurationSource;
    }
}
