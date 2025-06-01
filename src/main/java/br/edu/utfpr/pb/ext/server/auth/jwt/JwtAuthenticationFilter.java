package br.edu.utfpr.pb.ext.server.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_TOKEN_PREFIX = "Bearer ";
  private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final HandlerExceptionResolver handlerExceptionResolver;
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  /**
   * Realiza autenticação de requisições HTTP com base em token JWT.
   *
   * <p>Extrai o token JWT do cabeçalho Authorization, valida e autentica o usuário no contexto de segurança do Spring caso o token seja válido. Se o token estiver ausente, inválido, expirado ou se já houver autenticação, a requisição segue normalmente pela cadeia de filtros. Tokens expirados ou malformados resultam em resposta HTTP 401 com mensagem apropriada. Outras exceções são delegadas ao HandlerExceptionResolver.
   *
   * @param request requisição HTTP recebida
   * @param response resposta HTTP a ser enviada
   * @param filterChain cadeia de filtros a ser continuada
   * @throws ServletException se ocorrer um erro de servlet durante o processamento
   * @throws IOException se ocorrer um erro de I/O durante o processamento
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain)
      throws ServletException, IOException {
    final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

    if (authHeader == null || !authHeader.startsWith(BEARER_TOKEN_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final String jwt = authHeader.substring(BEARER_TOKEN_PREFIX.length());
      final String username = jwtService.extractUsername(jwt);

      if (username == null) {
        filterChain.doFilter(request, response);
        return;
      }

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication == null) {
        UserDetails userdetails = this.userDetailsService.loadUserByUsername(username);

        if (jwtService.isTokenValid(jwt, userdetails)) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userdetails, null, userdetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
      filterChain.doFilter(request, response);
    } catch (ExpiredJwtException e) {
      LOGGER.debug("Tentativa de login com token expirado");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Token expirado");
    } catch (MalformedJwtException e) {
      LOGGER.debug("Tentativa de login com token inválido");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Token inválido");

    } catch (Exception e) {
      handlerExceptionResolver.resolveException(request, response, null, e);
    }
  }
}
