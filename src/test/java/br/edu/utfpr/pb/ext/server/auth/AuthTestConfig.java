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

  public static final String TEST_EMAIL = "testuser@alunos.utfpr.edu.br";
  public static final String CODIGO_VALIDO = "123456";

  /**
   * Retorna um mock de EmailServiceImpl que simula o envio bem-sucedido de código de autenticação por e-mail no contexto "autenticacao".
   *
   * @return instância mockada de EmailServiceImpl que retorna uma resposta de sucesso ao chamar generateAndSendCode com o contexto "autenticacao"
   * @throws IOException se ocorrer um erro de E/S durante a criação do mock
   */
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

  /**
   * Fornece um mock de EmailCodeValidationService para testes de validação de códigos OTP por e-mail.
   *
   * O mock retorna {@code true} apenas quando o e-mail, o contexto e o código correspondem aos valores válidos de teste; para qualquer código inválido, retorna {@code false}, independentemente do e-mail ou contexto.
   *
   * @return instância mockada de EmailCodeValidationService com respostas previsíveis para cenários de teste
   */
  @Bean
  @Primary
  public EmailCodeValidationService emailCodeValidationServiceMock() {
    String autenticacao = "autenticacao";
    String codigoInvalido = "codigo-invalido";

    EmailCodeValidationService mockValidationService =
        Mockito.mock(EmailCodeValidationService.class);

    when(mockValidationService.validateCode(TEST_EMAIL, autenticacao, CODIGO_VALIDO))
        .thenReturn(true);
    when(mockValidationService.validateCode(anyString(), anyString(), eq(codigoInvalido)))
        .thenReturn(false);

    return mockValidationService;
  }

  /**
   * Fornece um mock de {@link EmailOtpAuthenticationProvider} para testes de autenticação OTP por e-mail.
   *
   * O mock autentica com sucesso apenas quando o e-mail e o código correspondem aos valores de teste predefinidos.
   * Caso contrário, lança exceções para simular falhas de autenticação, como código inválido ou usuário não encontrado.
   *
   * @param usuarioRepository repositório utilizado para buscar o usuário pelo e-mail durante a autenticação simulada
   * @return mock de {@code EmailOtpAuthenticationProvider} com comportamento controlado para cenários de teste
   */
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

              if (TEST_EMAIL.equals(email) && CODIGO_VALIDO.equals(code)) {
                Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                  UserDetails userDetails = userOpt.get();
                  return new UsernamePasswordAuthenticationToken(
                      userDetails, null, userDetails.getAuthorities());
                } else {
                  throw new org.springframework.security.core.userdetails.UsernameNotFoundException(
                      "Usuário nao encontrado");
                }
              }

              throw new org.springframework.security.authentication.BadCredentialsException(
                  "Código de verificação inválido ou expirado");
            });

    return mockProvider;
  }
}
