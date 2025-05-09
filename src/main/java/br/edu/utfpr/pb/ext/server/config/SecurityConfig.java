package br.edu.utfpr.pb.ext.server.config;

import br.edu.utfpr.pb.ext.server.auth.jwt.JwtAuthenticationFilter;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Classe de configuração do Spring Security */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final UsuarioRepository usuarioRepository;

  /**
   * Injeta a chave app.client.origins e os valores existentes separados por vírgula configurado no
   * yml
   */
  @Value("#{'${app.client.origins}'.split(',')}")
  private List<String> allowedOrigins;

  public SecurityConfig(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

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
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http.cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
        .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.GET, "/api/projeto/**")
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
                    .requestMatchers("/h2-console/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/usuarios/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
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
   * Configura as definições de CORS para a aplicação. Utiliza a propriedade {@code
   * react.client.origins} para definir as origens permitidas. Permite os métodos HTTP mais comuns e
   * cabeçalhos como Authorization e Content-Type. Permite o envio de credenciais.
   *
   * @return uma instância de {@link CorsConfigurationSource} configurada.
   */
  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  UserDetailsService userDetailsService() {
    return u ->
        usuarioRepository
            .findByEmail(u)
            .orElseThrow(() -> new UsernameNotFoundException("Email não cadastrado"));
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }
}
