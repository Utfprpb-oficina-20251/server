package br.edu.utfpr.pb.ext.server.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  @Value("${app.security.jwt.secret-key}")
  private String secretKey;

  @Getter
  @Value("${app.security.jwt.expiration-time}")
  private long expirationTime;

  Logger logger = LoggerFactory.getLogger(JwtService.class.getName());

  /**
   * Valida se a chave secreta decodificada possui pelo menos 64 bytes.
   *
   * <p>Lança uma exceção {@link IllegalStateException} se a chave for inválida para uso com HS512.
   */
  @PostConstruct
  private void validateSecretKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    if (keyBytes.length < 64) {
      throw new IllegalStateException(
          "Secret inválida. Deve ser formatado no padrão HS12 convertido em base64");
    }
  }

  /**
   * Extrai o nome de usuário (subject) de um token JWT.
   *
   * @param token o token JWT do qual o nome de usuário será extraído
   * @return o nome de usuário contido no subject do token, ou {@code null} se não for possível
   *     extrair
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extrai um valor específico dos claims de um token JWT usando a função fornecida.
   *
   * @param <T> tipo do valor extraído dos claims
   * @param token o token JWT do qual extrair os claims
   * @param claimsResolver função que recebe os claims e retorna o valor desejado
   * @return o valor extraído dos claims, ou {@code null} se o token for inválido
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    if (claims == null) {
      return null;
    }
    return claimsResolver.apply(claims);
  }

  /**
   * Gera um token JWT para o usuário especificado, sem incluir claims adicionais.
   *
   * @param userdetails detalhes do usuário para quem o token será gerado
   * @return o token JWT gerado
   */
  public String generateToken(UserDetails userdetails) {
    return generateToken(new HashMap<>(), userdetails);
  }

  /**
   * Gera um token JWT para o usuário especificado, incluindo claims adicionais.
   *
   * @param extraClaims mapa de claims extras a serem incluídos no token
   * @param userDetails detalhes do usuário para quem o token será gerado
   * @return o token JWT gerado como uma string
   */
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, expirationTime);
  }

  /**
   * Constrói um token JWT assinado com as claims fornecidas, usuário e tempo de expiração
   * especificados.
   *
   * @param extraClaims mapa de claims adicionais a serem incluídas no token
   * @param userDetails detalhes do usuário para definir o subject do token
   * @param expirationTime tempo de expiração em milissegundos a partir da emissão
   * @return o token JWT gerado como string
   */
  private String buildToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long expirationTime) {
    return Jwts.builder()
        .claims()
        .issuer("utfpr-pb-ext-server")
        .subject(userDetails.getUsername())
        .issuedAt(new java.util.Date(System.currentTimeMillis()))
        .expiration(new java.util.Date(System.currentTimeMillis() + expirationTime))
        .add(extraClaims)
        .and()
        .signWith(getSignInKey(), Jwts.SIG.HS512)
        .compact();
  }

  /**
   * Verifica se um token JWT é válido para o usuário fornecido.
   *
   * <p>O token é considerado válido se o nome de usuário extraído do token corresponder ao do
   * usuário informado e se o token não estiver expirado.
   *
   * @param token o token JWT a ser validado
   * @param userDetails os detalhes do usuário para validação
   * @return {@code true} se o token for válido para o usuário e não estiver expirado; caso
   *     contrário, {@code false}
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return username != null
        && (username.equals(userDetails.getUsername()))
        && !isTokenExpired(token);
  }

  /**
   * Verifica se o token JWT está expirado comparando a data de expiração com a data atual.
   *
   * @param token o token JWT a ser verificado
   * @return {@code true} se o token estiver expirado, caso contrário {@code false}
   */
  public boolean isTokenExpired(String token) {
    Date expiration = extractExpiration(token);
    return expiration == null || extractExpiration(token).before(new Date());
  }

  /**
   * Extrai a data de expiração de um token JWT.
   *
   * @param token o token JWT do qual extrair a data de expiração
   * @return a data de expiração do token, ou {@code null} se não for possível extrair
   */
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extrai todos os claims de um token JWT após verificar sua assinatura.
   *
   * @param token o token JWT a ser analisado
   * @return os claims extraídos do token, ou {@code null} se o token for inválido ou ocorrer erro
   *     na extração
   */
  private Claims extractAllClaims(String token) {
    try {
      return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
    } catch (JwtException e) {
      logger.error("Erro ao extrair claims do token: {}", e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      logger.error("Formato de claims inválido: {}", e.getMessage(), e);
    }
    return Jwts.claims().build();
  }

  /**
   * Decodifica a chave secreta em base64 e retorna uma instância de {@link SecretKey} para
   * assinatura HMAC SHA.
   *
   * @return chave secreta para assinatura de tokens JWT
   */
  private SecretKey getSignInKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }
}
