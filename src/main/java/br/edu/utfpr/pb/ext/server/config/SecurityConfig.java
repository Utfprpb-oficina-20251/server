package br.edu.utfpr.pb.ext.server.config;

import br.edu.utfpr.pb.ext.server.auth.jwt.JwtAuthenticationFilter;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
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
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Classe de configuração do Spring Security */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private final Environment environment;

  private final UsuarioRepository usuarioRepository;

  /**
   * Injeta a chave app.client.origins e os valores existentes separados por vírgula configurado no
   * yml
   */
  @Value("#{'${app.client.origins}'.split(',')}")
  private List<String> allowedOrigins;

  /**
   * Cria uma instância de configuração de segurança com as dependências necessárias.
   *
   * @param environment ambiente Spring utilizado para acessar propriedades e perfis ativos
   * @param usuarioRepository repositório para acesso aos dados de usuários
   */
  public SecurityConfig(Environment environment, UsuarioRepository usuarioRepository) {
    this.environment = environment;
    this.usuarioRepository = usuarioRepository;
  }

  /**
   * Configura a cadeia de filtros de segurança HTTP, definindo autenticação, autorização, CORS,
   * CSRF e política de sessão para a aplicação.
   *
   * <p>Permite acesso público a requisições GET em `/api/projeto/**` e a todas as rotas em
   * `/api/auth/**`. Restringe o acesso a rotas específicas conforme o papel do usuário, exige
   * autenticação para demais endpoints e permite acesso ao console H2 apenas em perfil de teste.
   * Define sessões como stateless, habilita CORS e adiciona filtro de autenticação JWT.
   *
   * @param http configuração de segurança HTTP do Spring
   * @return cadeia de filtros de segurança configurada
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
                    .access(isTestProfile())
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html/**")
                    .access(isTestProfile())
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * Retorna um AuthorizationManager que permite acesso apenas se o perfil ativo incluir "test".
   *
   * @return AuthorizationManager que concede autorização somente quando o perfil "test" está ativo.
   */
  private AuthorizationManager<RequestAuthorizationContext> isTestProfile() {
    return (authentication, context) ->
        Arrays.stream(environment.getActiveProfiles()).toList().contains("test")
            ? new AuthorizationDecision(true)
            : new AuthorizationDecision(false);
  }

  /**
   * Retorna um codificador de senhas que utiliza o algoritmo BCrypt para garantir o armazenamento
   * seguro das senhas dos usuários.
   *
   * @return instância de PasswordEncoder baseada em BCrypt
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Define a configuração de CORS para a aplicação, permitindo origens, métodos e cabeçalhos
   * específicos.
   *
   * <p>Utiliza as origens permitidas definidas na propriedade {@code app.client.origins}, autoriza
   * os métodos HTTP GET, POST, PUT, DELETE e OPTIONS, e os cabeçalhos Authorization, Content-Type,
   * X-Requested-With e Accept. Permite o envio de credenciais.
   *
   * @return uma instância configurada de {@link CorsConfigurationSource}
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

  /**
   * Fornece um serviço de autenticação que carrega detalhes do usuário pelo e-mail.
   *
   * @return um UserDetailsService que busca usuários no repositório pelo e-mail e lança
   *     UsernameNotFoundException se não encontrado
   */
  @Bean
  UserDetailsService userDetailsService() {
    return u ->
        usuarioRepository
            .findByEmail(u)
            .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
  }

  /**
   * Fornece o bean {@link AuthenticationManager} a partir da configuração de autenticação do
   * Spring.
   *
   * @param config configuração de autenticação do Spring Security
   * @return instância do {@link AuthenticationManager} configurada
   * @throws Exception se ocorrer erro ao obter o gerenciador de autenticação
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Cria e configura um AuthenticationProvider baseado em DAO com UserDetailsService e
   * PasswordEncoder personalizados.
   *
   * @return o AuthenticationProvider configurado para autenticação de usuários.
   */
  @Bean
  AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }
}
