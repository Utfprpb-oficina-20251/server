package br.edu.utfpr.pb.ext.server.config;

import br.edu.utfpr.pb.ext.server.auth.jwt.JwtService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class JwtTestConfig {
  @Bean
  @Primary
  public JwtService jwtServiceMock() {
    return Mockito.mock(JwtService.class);
  }
}
