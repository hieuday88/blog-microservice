package com.blog.auth.config;

import com.blog.auth.security.JwtAuthenticationFilter; // Import Filter của bạn
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Import class này để xác định vị trí đặt Filter

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // 1. Khai báo biến Filter

    // 2. Inject Filter vào Constructor (Bổ sung tham số thứ 2)
    public SecurityConfig(UserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // Bean fix lỗi StackOverflow (Giữ nguyên của bạn)
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authorize) -> authorize
                        // Mở API Auth
                        .requestMatchers("/api/auth/**").permitAll()

                        // Mở Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()

                        // Các request khác bắt buộc login
                        .anyRequest().authenticated()
                );

        // 3. QUAN TRỌNG NHẤT: Thêm dòng này để kích hoạt "Ông bảo vệ" (Filter)
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}