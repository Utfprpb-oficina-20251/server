package br.edu.utfpr.pb.ext.server.config;

import br.edu.utfpr.pb.ext.server.auth.jwt.JwtAuthenticationFilter;
import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationProvider;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

  private final EmailOtpAuthenticationProvider emailOtpAuthenticationProvider;

  /**
   * Injeta a chave app.client.origins e os valores existentes separados por vírgula configurado no
   * yml
   */
  @Value("#{'${app.client.origins}'.split(',')}")
  private List<String> allowedOrigins;

  @Value("${app.swagger.enabled}")
  private boolean isSwaggerEnabled;

  /**
   * Inicializa a configuração de segurança com as dependências para autenticação e acesso ao ambiente.
   *
   * @param environment ambiente Spring usado para acessar propriedades e perfis ativos
   * @param usuarioRepository repositório para consulta de dados de usuários
   * @param emailOtpAuthenticationProvider provedor de autenticação via OTP por e-mail
   */
  public SecurityConfig(
      Environment environment,
      UsuarioRepository usuarioRepository,
      EmailOtpAuthenticationProvider emailOtpAuthenticationProvider) {
    this.environment = environment;
    this.usuarioRepository = usuarioRepository;
    this.emailOtpAuthenticationProvider = emailOtpAuthenticationProvider;
  }

  /**
   * Configura a cadeia de filtros de segurança HTTP da aplicação, incluindo autenticação,
   * autorização, CORS, CSRF e gerenciamento de sessão.
   *
   * <p>Define regras de acesso para diferentes endpoints, permitindo acesso público a rotas
   * específicas, restringindo outras por perfil de usuário e ambiente, e exigindo autenticação para
   * as demais. O gerenciamento de sessão é stateless e um filtro de autenticação JWT é adicionado à
   * cadeia.
   *
   * @param http configuração de segurança HTTP do Spring
   * @param jwtAuthenticationFilter filtro de autenticação JWT a ser inserido na cadeia
   * @return a cadeia de filtros de segurança configurada
   * @throws Exception se ocorrer erro na configuração da segurança
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http.cors(c -> c.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
        .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.GET, "/api/projeto/**")
                    .permitAll()
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/h2-console/**")
                    .access(isTestProfile())
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html/**")
                    .access(isSwaggerEnabled())
                    .requestMatchers(HttpMethod.POST, "/api/usuarios/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/projeto/**")
                    .hasRole("SERVIDOR")
                    .requestMatchers(HttpMethod.OPTIONS, "/**") // CORS preflight
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(emailOtpAuthenticationProvider)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * Cria um AuthorizationManager que permite acesso apenas se o perfil ativo do Spring incluir "test".
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
   * Cria um AuthorizationManager que concede acesso somente quando o Swagger está habilitado na
   * configuração da aplicação.
   *
   * @return AuthorizationManager que autoriza o acesso caso o Swagger esteja ativado.
   */
  private AuthorizationManager<RequestAuthorizationContext> isSwaggerEnabled() {
    return (authentication, context) ->
        isSwaggerEnabled ? new AuthorizationDecision(true) : new AuthorizationDecision(false);
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
    configuration.setAllowedOriginPatterns(allowedOrigins);
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
   * Expõe o bean {@link AuthenticationManager} configurado pelo Spring Security.
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
}
