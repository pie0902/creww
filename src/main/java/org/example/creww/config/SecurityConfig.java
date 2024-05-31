package org.example.creww.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

            .csrf(csrf -> csrf.disable()) // CSRF 보호 기능 비활성화
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
            .httpBasic(httpBasic -> httpBasic.disable()) // HTTP 기본 인증 비활성화
            .sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
            .authorizeHttpRequests(authorize -> authorize
                // 모든 요청을 허용
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 크로스 오리진 요청 시 쿠키를 포함시키기 위해 true로 설정
        config.addAllowedOrigin("http://localhost:3000"); // 개발 중인 로컬 호스트의 프론트엔드 주소
        config.addAllowedOrigin("http://localhost:3001"); // 필요한 경우 다른 로컬 개발 주소도 추가
        config.addAllowedOrigin("http://creww.duckdns.org"); // 프로덕션 환경 도메인 추가
        config.addAllowedOrigin("https://creww.duckdns.org"); // HTTPS를 사용하는 경우 추가
        config.addAllowedHeader("*"); // 모든 헤더 허용
        config.addAllowedMethod("*"); // 모든 HTTP 메소드 허용
        config.addExposedHeader("Authorization"); // Authorization 헤더 노출
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
