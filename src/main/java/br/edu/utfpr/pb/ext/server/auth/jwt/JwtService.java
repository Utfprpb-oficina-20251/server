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
   * Valida se a chave secreta decodificada possui pelo menos 64 bytes, requisito para uso seguro do
   * algoritmo HS512.
   *
   * <p>Lança uma {@link IllegalStateException} se a chave for inválida.
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
   * @param token token JWT do qual o nome de usuário será extraído
   * @return o nome de usuário presente no subject do token, ou {@code null} se não for possível
   *     extrair
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extrai um valor específico dos claims de um token JWT utilizando a função fornecida.
   *
   * @param <T> tipo do valor a ser extraído dos claims
   * @param token token JWT do qual extrair as informações
   * @param claimsResolver função que recebe os claims e retorna o valor desejado
   * @return o valor extraído dos claims, ou {@code null} se o token for inválido ou os claims não
   *     puderem ser obtidos
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    if (claims == null) {
      return null;
    }
    return claimsResolver.apply(claims);
  }

  /**
   * Gera um token JWT para o usuário informado, sem claims extras.
   *
   * @param userdetails detalhes do usuário para quem o token será gerado
   * @return o token JWT assinado correspondente ao usuário
   */
  public String generateToken(UserDetails userdetails) {
    return generateToken(new HashMap<>(), userdetails);
  }

  /**
   * Gera um token JWT assinado para o usuário especificado, incluindo claims extras fornecidos.
   *
   * @param extraClaims mapa de claims adicionais a serem inseridos no token
   * @param userDetails detalhes do usuário para quem o token será emitido
   * @return o token JWT gerado como string
   */
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, expirationTime);
  }

  /**
   * Gera um token JWT assinado contendo as claims fornecidas, o usuário especificado e o tempo de
   * expiração definido.
   *
   * @param extraClaims mapa de claims adicionais a serem incluídas no token
   * @param userDetails detalhes do usuário que será definido como subject do token
   * @param expirationTime tempo de expiração em milissegundos a partir do momento de emissão
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
   * Verifica se o token JWT pertence ao usuário especificado e se ainda está dentro do prazo de
   * validade.
   *
   * <p>O token é considerado válido se o nome de usuário extraído corresponder ao do usuário
   * fornecido e se não estiver expirado.
   *
   * @param token token JWT a ser verificado
   * @param userDetails detalhes do usuário esperado
   * @return {@code true} se o token for válido e não expirado para o usuário; caso contrário,
   *     {@code false}
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return username != null
        && (username.equals(userDetails.getUsername()))
        && !isTokenExpired(token);
  }

  /**
   * Verifica se o token JWT está expirado em relação à data e hora atual.
   *
   * <p>Retorna {@code true} se o token estiver expirado ou se a data de expiração não puder ser
   * extraída; caso contrário, retorna {@code false}.
   *
   * @param token token JWT a ser verificado
   * @return {@code true} se o token estiver expirado ou inválido, {@code false} caso contrário
   */
  public boolean isTokenExpired(String token) {
    Date expiration = extractExpiration(token);
    return expiration == null || extractExpiration(token).before(new Date());
  }

  /**
   * Obtém a data de expiração de um token JWT.
   *
   * @param token token JWT do qual extrair a data de expiração
   * @return a data de expiração, ou {@code null} se não for possível extrair
   */
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extrai todos os claims de um token JWT após validar sua assinatura.
   *
   * @param token o token JWT a ser analisado
   * @return os claims extraídos do token; retorna um objeto vazio se o token for inválido ou
   *     ocorrer erro na extração
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
   * @return a chave secreta derivada da configuração, utilizada para assinar e validar tokens JWT
   */
  private SecretKey getSignInKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }
}
