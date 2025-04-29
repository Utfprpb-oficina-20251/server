package br.edu.utfpr.pb.ext.server.config;

import br.edu.utfpr.pb.ext.server.security.Roles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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
        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        )
                .authorizeHttpRequests(authorize -> authorize
                        // Endpoints de acesso públicos
                        .requestMatchers(HttpMethod.GET, "/api/projects/**").permitAll() // endPoint projects como pedido no pext-12
                        .requestMatchers("/api/auth/**").permitAll() // endpoint padrao

                        // Endpoints de acesso protegidos por role
                        .requestMatchers("/api/users/**").hasRole("ADMINISTRADOR") //endpoint de acesso de cadastro de usuários
                        .requestMatchers("/api/administrador/**").hasRole("ADMINISTRADOR") //endpoints do administradors
                        .requestMatchers("/api/servidor/**").hasRole("SERVIDOR") //endpoints dos servidores
                        .requestMatchers("/api/estudante/**").hasRole("ESTUDANTE") //endpoints dos estudantes

                        // Todos os outros endpoints requerem autenticação
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // TEMPORARIO, LEMBRAR DE REMOVER
        // Substituir por JWT e banco de dados
        UserDetails administrador = User.builder()
                .username("administrador")
                .password(passwordEncoder().encode("administrador123"))
                .roles(Roles.ADMINISTRADOR.getAuthority())
                .build();

        UserDetails servidor = User.builder()
                .username("servidor")
                .password(passwordEncoder().encode("servidor123"))
                .roles(Roles.SERVIDOR.getAuthority())
                .build();

        UserDetails estudante = User.builder()
                .username("estudante")
                .password(passwordEncoder().encode("estudante123"))
                .roles(Roles.ESTUDANTE.getAuthority())
                .build();

        return new InMemoryUserDetailsManager(administrador, servidor, estudante);
    }

    @Bean
    public PasswordEncoder passwordEncoder() { //retorna criptografada a senha
        return new BCryptPasswordEncoder();
    }
}