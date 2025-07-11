package br.edu.utfpr.pb.ext.server.config;

import br.edu.utfpr.pb.ext.server.auth.jwt.JwtAuthenticationFilter;
import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationProvider;
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Classe de configuração do Spring Security */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  private final Environment environment;
  private final EmailOtpAuthenticationProvider emailOtpAuthenticationProvider;

  private final EntryPointUnauthorizedHandler entryPointUnauthorizedHandler;

  /**
   * Injeta a chave app.client.origins e os valores existentes separados por vírgula configurado no
   * yml
   */
  @Value("#{'${app.client.origins}'.split(',')}")
  private List<String> allowedOrigins;

  @Value("${app.swagger.enabled}")
  private boolean isSwaggerEnabled;

  /**
   * Cria a configuração de segurança com as dependências necessárias para autenticação e acesso ao
   * ambiente.
   *
   * @param environment ambiente Spring para acesso a propriedades e perfis ativos
   * @param emailOtpAuthenticationProvider provedor de autenticação OTP por e-mail
   */
  public SecurityConfig(
      Environment environment,
      EmailOtpAuthenticationProvider emailOtpAuthenticationProvider,
      EntryPointUnauthorizedHandler entryPointUnauthorizedHandler) {
    this.environment = environment;
    this.emailOtpAuthenticationProvider = emailOtpAuthenticationProvider;
    this.entryPointUnauthorizedHandler = entryPointUnauthorizedHandler;
  }

  /**
   * Configura a cadeia de filtros de segurança HTTP da aplicação, incluindo autenticação JWT,
   * autorização baseada em perfis, CORS, CSRF e gerenciamento de sessão sem estado.
   *
   * <p>Define regras de acesso público e restrito para diferentes endpoints, considerando métodos
   * HTTP, perfis ativos e configuração do Swagger. Adiciona o filtro de autenticação JWT antes do
   * filtro padrão de autenticação por usuário e senha.
   *
   * @param http configuração de segurança HTTP do Spring
   * @param jwtAuthenticationFilter filtro de autenticação JWT a ser adicionado à cadeia
   * @return cadeia de filtros de segurança configurada
   * @throws Exception se ocorrer erro na configuração da segurança
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http.cors(c -> c.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
        .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
        .exceptionHandling(
            exceptions -> exceptions.authenticationEntryPoint(entryPointUnauthorizedHandler))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.GET, "/api/projeto/meusprojetos")
                    .authenticated()
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
                    .requestMatchers(HttpMethod.GET, "/api/usuarios/executores")
                    .hasRole("SERVIDOR")
                    .requestMatchers(HttpMethod.GET, "/api/sugestao/minhas-indicacoes")
                    .hasRole("SERVIDOR")
                    .requestMatchers(HttpMethod.GET, "/api/departamento/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/curso/**")
                    .permitAll()
                    .requestMatchers("/api/departamento/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/curso/**")
                    .hasRole("ADMIN")
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
   * Retorna um AuthorizationManager que concede acesso somente quando o perfil ativo do Spring
   * inclui "test".
   *
   * @return AuthorizationManager que autoriza requisições apenas se o perfil "test" estiver ativo.
   */
  private AuthorizationManager<RequestAuthorizationContext> isTestProfile() {
    return (authentication, context) ->
        Arrays.stream(environment.getActiveProfiles()).toList().contains("test")
            ? new AuthorizationDecision(true)
            : new AuthorizationDecision(false);
  }

  /**
   * Retorna um AuthorizationManager que permite acesso apenas se o Swagger estiver habilitado na
   * configuração da aplicação.
   *
   * @return AuthorizationManager que autoriza o acesso quando o Swagger está ativado.
   */
  private AuthorizationManager<RequestAuthorizationContext> isSwaggerEnabled() {
    return (authentication, context) ->
        isSwaggerEnabled ? new AuthorizationDecision(true) : new AuthorizationDecision(false);
  }

  /**
   * Cria e retorna a configuração de CORS para a aplicação.
   *
   * <p>Permite apenas as origens, métodos HTTP e cabeçalhos especificados nas propriedades da
   * aplicação, além de suportar o envio de credenciais.
   *
   * @return a configuração de CORS aplicada a todos os endpoints
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
   * Fornece o bean {@link AuthenticationManager} configurado pelo Spring Security.
   *
   * @param config configuração de autenticação do Spring Security
   * @return instância do {@link AuthenticationManager} obtida da configuração
   * @throws Exception se ocorrer falha ao recuperar o gerenciador de autenticação
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
