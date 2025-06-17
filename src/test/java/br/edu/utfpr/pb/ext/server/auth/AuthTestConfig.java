package br.edu.utfpr.pb.ext.server.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationProvider;
import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationToken;
import br.edu.utfpr.pb.ext.server.email.EmailCodeValidationService;
import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import br.edu.utfpr.pb.ext.server.email.impl.EmailServiceImpl;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioServiceImpl;
import com.sendgrid.Response;
import java.io.IOException;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@TestConfiguration
public class AuthTestConfig {

  public static final String TEST_EMAIL = "testuser@alunos.utfpr.edu.br";
  public static final String CODIGO_VALIDO = "123456";

  /**
   * Cria um mock de EmailServiceImpl que simula o envio bem-sucedido de código de autenticação por
   * e-mail.
   *
   * <p>O mock retorna sempre uma resposta de sucesso (HTTP 202) ao chamar generateAndSendCode com
   * qualquer e-mail e o tipo de código OTP_AUTENTICACAO.
   *
   * @return instância mockada de EmailServiceImpl para uso em testes
   * @throws IOException se ocorrer um erro de E/S durante a criação do mock
   */
  @Bean
  @Primary
  public EmailServiceImpl emailServiceMock() throws IOException {
    EmailServiceImpl mockEmailService = Mockito.mock(EmailServiceImpl.class);

    // Mock generateAndSendCode to return a successful response
    Response mockResponse = new Response(202, "Success", null);
    when(mockEmailService.generateAndSendCode(anyString(), eq(TipoCodigo.OTP_AUTENTICACAO)))
        .thenReturn(mockResponse);

    return mockEmailService;
  }

  /**
   * Cria um mock de EmailCodeValidationService para testes de validação de código OTP por e-mail.
   *
   * <p>O mock retorna {@code true} apenas quando o e-mail, o tipo de código e o código fornecidos
   * correspondem aos valores de teste válidos; para qualquer código inválido, retorna {@code
   * false}, independentemente dos demais parâmetros.
   *
   * @return um mock de EmailCodeValidationService com respostas controladas para cenários de
   *     autenticação por e-mail em testes
   */
  @Bean
  @Primary
  public EmailCodeValidationService emailCodeValidationServiceMock() {
    String codigoInvalido = "codigo-invalido";

    EmailCodeValidationService mockValidationService =
        Mockito.mock(EmailCodeValidationService.class);

    when(mockValidationService.validateCode(TEST_EMAIL, TipoCodigo.OTP_AUTENTICACAO, CODIGO_VALIDO))
        .thenReturn(true);
    when(mockValidationService.validateCode(anyString(), any(TipoCodigo.class), eq(codigoInvalido)))
        .thenReturn(false);

    return mockValidationService;
  }

  /**
   * Fornece um mock de {@link EmailOtpAuthenticationProvider} para testes, simulando o fluxo de
   * autenticação OTP por e-mail.
   */
  @Bean
  @Primary
  public EmailOtpAuthenticationProvider emailOtpAuthenticationProviderMock(
      UsuarioServiceImpl userDetailsService) {
    EmailOtpAuthenticationProvider mockProvider =
        Mockito.mock(EmailOtpAuthenticationProvider.class);

    // Mock authenticate to return a valid authentication for specific test cases
    when(mockProvider.authenticate(any(EmailOtpAuthenticationToken.class)))
        .thenAnswer(
            invocation -> {
              EmailOtpAuthenticationToken token = invocation.getArgument(0);
              String email = token.getPrincipal().toString();
              String code = token.getCredentials().toString();

              if (TEST_EMAIL.equals(email) && CODIGO_VALIDO.equals(code)) {
                try {
                  UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                  return new UsernamePasswordAuthenticationToken(
                      userDetails, null, userDetails.getAuthorities());
                } catch (UsernameNotFoundException e) {
                  throw new UsernameNotFoundException("Usuário nao encontrado");
                }
              }

              throw new BadCredentialsException("Código de verificação inválido ou expirado");
            });

    return mockProvider;
  }
}
