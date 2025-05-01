package br.edu.utfpr.pb.ext.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/** Classe de configuração do Spring Security */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Value("${spring.security.user.name")
  private String username;

  @Value("${spring.security.user.password}")
  private String password;

  @Value("${spring.security.user.roles}")
  private String role;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
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

  /**
   * Metodo que retorna o codificador de senha do tipo PasswordEncoder, utilizado para gerar hash de
   * senhas antes do armazenamento
   *
   * @return Instância do BCryptPasswordEncoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Metodo para configuração de usuário padrão injetável, deverá ser removido após término de
   * implementação de autorização via JWT
   *
   * @deprecated Remoção pendente durante implementação JWT, só para fins de testes enquanto o
   *     recurso não é terminado
   * @param passwordEncoder Função que gera hash da senha utilizando padrão bcrypt
   * @return Instância de InMemoryUserDetailsManager com o usuário definido
   */
  @Bean
  @Deprecated(forRemoval = true)
  public UserDetailsService usuarioPadraoAteImplementacaoJWT(PasswordEncoder passwordEncoder) {
    UserDetails user =
        User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .roles(role)
            .build();
    return new InMemoryUserDetailsManager(user);
  }
}
