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

  @PostConstruct
  private void validateSecretKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    if (keyBytes.length < 64) {
      throw new IllegalStateException(
          "Secret inválida. Deve ser formatado no padrão HS12 convertido em base64");
    }
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(UserDetails userdetails) {
    return generateToken(new HashMap<>(), userdetails);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, expirationTime);
  }

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

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    try {
      return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
    } catch (JwtException e) {
      logger.error("Erro ao extrair claims do token: {}", e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      logger.error("Formato de claims inválido: {}", e.getMessage(), e);
    }
    return null;
  }

  private SecretKey getSignInKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }
}
