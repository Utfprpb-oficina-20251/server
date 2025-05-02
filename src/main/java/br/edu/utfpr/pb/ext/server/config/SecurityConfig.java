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
  @Value("${spring.security.user.name}")
  private String username;

  @Value("${spring.security.user.password}")
  private String password;

  @Value("${spring.security.user.roles}")
  private String role;

  /**
   * Configura a cadeia de filtros de segurança HTTP para a aplicação.
   *
   * <p>Define regras de autorização para diferentes endpoints da API, desabilita CSRF para rotas
   * sob `/api/**` e exige autenticação ou papéis específicos conforme o caminho. Permite acesso
   * público a requisições GET em `/api/projects/**` e a todas as rotas em `/api/auth/**`. Define a
   * política de sessão como stateless.
   *
   * @param http objeto de configuração de segurança HTTP do Spring
   * @return a cadeia de filtros de segurança configurada
   * @throws Exception se ocorrer erro na configuração de segurança
   */
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
   * Fornece um codificador de senhas baseado em BCrypt para hashing seguro de senhas.
   *
   * @return uma instância de BCryptPasswordEncoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Cria um usuário padrão em memória para autenticação temporária até a implementação completa do
   * JWT.
   *
   * @deprecated Este metodo será removido após a conclusão da autorização baseada em JWT; utilizado
   *     apenas para testes temporários.
   * @param passwordEncoder Codificador de senha utilizado para gerar o hash da senha do usuário.
   * @return Um InMemoryUserDetailsManager contendo o usuário padrão configurado.
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
