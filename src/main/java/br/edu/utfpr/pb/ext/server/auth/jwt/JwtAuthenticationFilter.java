package br.edu.utfpr.pb.ext.server.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
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
  private final HandlerExceptionResolver handlerExceptionResolver;
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  /**
   * Intercepta requisições HTTP e realiza autenticação baseada em JWT.
   *
   * Extrai o token JWT do cabeçalho Authorization, valida o token e, se válido, autentica o usuário no contexto de segurança do Spring. Caso o token esteja ausente, inválido ou a autenticação já exista, a requisição segue normalmente pela cadeia de filtros. Exceções durante o processo são tratadas pelo HandlerExceptionResolver.
   *
   * @param request requisição HTTP recebida
   * @param response resposta HTTP a ser enviada
   * @param filterChain cadeia de filtros a ser continuada
   * @throws ServletException se ocorrer um erro de servlet durante o processamento
   * @throws IOException se ocorrer um erro de I/O durante o processamento
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

    if (authHeader == null || !authHeader.startsWith(BEARER_TOKEN_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final String jwt = authHeader.substring(BEARER_TOKEN_PREFIX.length());
      final String username = jwtService.extractUsername(jwt);

      if (authHeader.substring(BEARER_TOKEN_PREFIX.length()).trim().isEmpty() || username == null) {
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
    } catch (Exception e) {
      handlerExceptionResolver.resolveException(request, response, null, e);
    }
  }
}
