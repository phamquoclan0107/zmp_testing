package com.stu.attendance.config;

import com.stu.attendance.security.CustomUserDetailsService;
import com.stu.attendance.security.JwtAuthenticationEntryPoint;
import com.stu.attendance.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean

    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF vì chúng ta sử dụng JWT
                .csrf(csrf -> csrf.disable())
                // Xử lý lỗi unauthorized
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                )
                // Cấu hình stateless session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Cho phép tất cả các request mà không cần xác thực
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // Cho phép tất cả các yêu cầu mà không cần xác thực
                );

        // Thêm filter JWT
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Sử dụng authentication provider
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                // Tắt CSRF vì chúng ta sử dụng JWT
//                .csrf(csrf -> csrf.disable())
//                // Xử lý lỗi unauthorized
//                .exceptionHandling(exception -> exception
//                        .authenticationEntryPoint(unauthorizedHandler)
//                )
//                // Cấu hình stateless session
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                // Cấu hình quyền truy cập
//                .authorizeHttpRequests(auth -> auth
//                        // Cho phép truy cập vào các API công khai không cần xác thực
//                        .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/reset-password").permitAll()
//                        // API Swagger/API docs
//                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
//                        // Tất cả request khác đều yêu cầu xác thực
//                        .anyRequest().authenticated()
//                );
//
//        // Thêm filter JWT
//        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        // Sử dụng authentication provider
//        http.authenticationProvider(authenticationProvider());
//
//        return http.build();
//    }
}