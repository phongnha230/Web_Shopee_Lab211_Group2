package com.shoppeclone.backend.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())

                                // ✅ DÙNG CorsConfig chung
                                .cors(cors -> {
                                })

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/me").authenticated()
                                                .requestMatchers(
                                                                "/api/auth/**",
                                                                "/api/upload/**", // Allow image upload (Cloudinary)
                                                                "/api/shop/upload-id-card", // Allow image upload for
                                                                                            // testing
                                                                "/api/webhooks/**") // Allow Webhooks explicitly
                                                .permitAll()

                                                // ✅ Allow Public GET Access (View Products, Categories, Shops,
                                                // Vouchers, Flash Sales countdown)
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/api/products/**",
                                                                "/api/flash-sales/**",
                                                                "/api/categories/**",
                                                                "/api/shop/**",
                                                                "/api/reviews/**",
                                                                "/api/shipping-providers/**",
                                                                "/api/vouchers",
                                                                "/api/vouchers/code/**",
                                                                "/api/debug/**")
                                                .permitAll()
                                                // Allow POST fix-category for dev (fix product category so vouchers
                                                // apply)
                                                .requestMatchers(org.springframework.http.HttpMethod.POST,
                                                                "/api/debug/products/*/fix-category")
                                                .permitAll()
                                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                                                .permitAll() // ✅ Allow CORS
                                                             // Preflight
                                                .requestMatchers("/api/**").authenticated() // Secure other APIs
                                                .anyRequest().permitAll()) // Allow static resources or others

                                // ✅ JWT → STATELESS
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // ✅ JWT FILTER
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}
