package br.edu.utfpr.pb.ext.server.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationProvider;
import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationToken;
import br.edu.utfpr.pb.ext.server.email.EmailCodeValidationService;
import br.edu.utfpr.pb.ext.server.email.impl.EmailServiceImpl;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import com.sendgrid.Response;
import java.io.IOException;
import java.util.Optional;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

@TestConfiguration
public class AuthTestConfig {

  @Bean
  @Primary
  public EmailServiceImpl emailServiceMock() throws IOException {
    EmailServiceImpl mockEmailService = Mockito.mock(EmailServiceImpl.class);

    // Mock generateAndSendCode to return a successful response
    Response mockResponse = new Response(202, "Success", null);
    when(mockEmailService.generateAndSendCode(anyString(), eq("autenticacao")))
        .thenReturn(mockResponse);

    return mockEmailService;
  }

  @Bean
  @Primary
  public EmailCodeValidationService emailCodeValidationServiceMock() {
    EmailCodeValidationService mockValidationService =
        Mockito.mock(EmailCodeValidationService.class);

    // Mock validateCode to return true for specific test cases and false otherwise
    when(mockValidationService.validateCode(
            "testuser@alunos.utfpr.edu.br", "autenticacao", "123456"))
        .thenReturn(true);
    when(mockValidationService.validateCode(anyString(), anyString(), eq("codigo-invalido")))
        .thenReturn(false);

    return mockValidationService;
  }

  @Bean
  @Primary
  public EmailOtpAuthenticationProvider emailOtpAuthenticationProviderMock(
      UsuarioRepository usuarioRepository) {
    EmailOtpAuthenticationProvider mockProvider =
        Mockito.mock(EmailOtpAuthenticationProvider.class);

    // Mock authenticate to return a valid authentication for specific test cases
    when(mockProvider.authenticate(any(EmailOtpAuthenticationToken.class)))
        .thenAnswer(
            invocation -> {
              EmailOtpAuthenticationToken token = invocation.getArgument(0);
              String email = token.getPrincipal().toString();
              String code = token.getCredentials().toString();

              if ("testuser@alunos.utfpr.edu.br".equals(email) && "123456".equals(code)) {
                Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                  UserDetails userDetails = userOpt.get();
                  return new UsernamePasswordAuthenticationToken(
                      userDetails, null, userDetails.getAuthorities());
                }
              }

              throw new org.springframework.security.authentication.BadCredentialsException(
                  "Código de verificação inválido ou expirado");
            });

    return mockProvider;
  }
}
