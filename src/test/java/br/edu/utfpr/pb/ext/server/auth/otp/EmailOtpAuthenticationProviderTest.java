package br.edu.utfpr.pb.ext.server.auth.otp;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.email.EmailCodeValidationService;
import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import br.edu.utfpr.pb.ext.server.usuario.IUsuarioService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import java.util.HashSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailOtpAuthenticationProviderTest {

  @Mock EmailCodeValidationService emailCodeValidationService;
  @Mock IUsuarioService usuarioService;
  @InjectMocks EmailOtpAuthenticationProvider provider;

  @Test
  @DisplayName("Authentication should activate user when OTP is valid")
  void authenticate_whenValidOtp_shouldActivateUser() {
    // Arrange
    String email = "test@alunos.utfpr.edu.br";
    String code = "123456";
    Usuario usuarioInativo =
        Usuario.builder().email(email).ativo(false).authorities(new HashSet<>()).build();

    when(emailCodeValidationService.validateCode(email, TipoCodigo.OTP_AUTENTICACAO, code))
        .thenReturn(true);
    when(usuarioService.loadUserByUsername(email)).thenReturn(usuarioInativo);

    doAnswer(
            invocation -> {
              usuarioInativo.setAtivo(true);
              return null;
            })
        .when(usuarioService)
        .ativarUsuario(email);

    // Act
    EmailOtpAuthenticationToken token = new EmailOtpAuthenticationToken(email, code);
    provider.authenticate(token);

    // Assert
    verify(usuarioService).ativarUsuario(email);
    verify(usuarioService).loadUserByUsername(email);
    assertTrue(
        usuarioInativo.isAtivo(), "Usuário deve estar ativo após validação de OTP bem sucedida");
  }
}
