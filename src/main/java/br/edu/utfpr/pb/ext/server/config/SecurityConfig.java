package br.edu.utfpr.pb.ext.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/*
 * Define endpoints publicos e restringe por cargos
 * Define politica stateless
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.GET, "/api/projects/**")
                    .permitAll()
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/api/users/**")
                    .hasRole("ADMINISTRADOR")
                    .requestMatchers("/api/administrador/**")
                    .hasRole("ADMINISTRADOR")
                    .requestMatchers("/api/servidor/**")
                    .hasRole("SERVIDOR")
                    .requestMatchers("/api/estudante/**")
                    .hasRole("ESTUDANTE")
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
