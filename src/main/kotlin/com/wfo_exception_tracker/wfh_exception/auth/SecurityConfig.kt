package com.wfo_exception_tracker.wfh_exception.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
class SecurityConfig(
    private val jwtFilter: JwtFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors {  }
            .authorizeHttpRequests { auth -> auth
                .requestMatchers("/auth/**", "/uploads/**").permitAll()
                .anyRequest().authenticated()
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        return http.build()
    }

    @Bean
    fun authenticationManager(configuration: AuthenticationConfiguration): AuthenticationManager {
        return configuration.authenticationManager
    }
}