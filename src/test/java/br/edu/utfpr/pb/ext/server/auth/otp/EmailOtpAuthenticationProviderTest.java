package br.edu.utfpr.pb.ext.server.auth.otp;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.email.EmailCodeValidationService;
import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioServiceImpl;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailOtpAuthenticationProviderTest {

  @Mock EmailCodeValidationService emailCodeValidationService;
  @Mock UsuarioRepository usuarioRepository;
  @InjectMocks EmailOtpAuthenticationProvider provider;

  private UsuarioServiceImpl usuarioDetailsService;

  @BeforeEach
  void setUp() {
    usuarioDetailsService = new UsuarioServiceImpl(usuarioRepository);
    // Inject the real service into the provider
    ReflectionTestUtils.setField(provider, "detailsService", usuarioDetailsService);
  }

  @Test
  @DisplayName("Authentication should activate user when OTP is valid")
  void authenticate_whenValidOtp_shouldActivateUser() {
    String email = "test@alunos.utfpr.edu.br";
    String code = "123456";

    Usuario usuario =
        Usuario.builder().email(email).ativo(false).authorities(new HashSet<>()).build();

    when(emailCodeValidationService.validateCode(email, TipoCodigo.OTP_AUTENTICACAO, code))
        .thenReturn(true);
    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    EmailOtpAuthenticationToken token = new EmailOtpAuthenticationToken(email, code);

    provider.authenticate(token);

    // Verify the user was actually activated
    assertTrue(usuario.isAtivo());
    verify(usuarioRepository).save(usuario);
  }
}
