package br.edu.utfpr.pb.ext.server.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.authority.Authority;
import br.edu.utfpr.pb.ext.server.usuario.authority.AuthorityRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock UsuarioRepository usuarioRepository;
  @Mock AuthorityRepository authorityRepository;
  @InjectMocks AuthService authService;

  @Test
  @DisplayName(
      "Solicitar código OTP para um e-mail que ocorrer erro deve retornar InternalServerError")
  void solicitarCodigoOtp_whenErroOcorrer_ShouldRetornarInternalServerError() {
    String email = "testuser@alunos.utfpr.edu.br";
    when(usuarioRepository.findByEmail(email)).thenThrow(new RuntimeException());
    assertThrows(ResponseStatusException.class, () -> authService.solicitarCodigoOtp(email));
  }

  @Test
  void cadastro_whenUsuarioNaoTemAlunoNoDominioUtfpr_deveRetornarUsuarioComRoleServidor() {
    CadastroUsuarioDTO cadastroDTO =
        CadastroUsuarioDTO.builder()
            .nome("testuser")
            .email("testuser@utfpr.edu.br")
            .registro("12345678901")
            .build();
    when(authorityRepository.findByAuthority("ROLE_SERVIDOR"))
        .thenReturn(Optional.of(Authority.builder().authority("ROLE_SERVIDOR").build()));
    authService.cadastro(cadastroDTO);

    ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
    verify(usuarioRepository, times(1)).save(captor.capture());

    assertEquals(1, captor.getValue().getAuthorities().size());
    assertEquals(
        "ROLE_SERVIDOR", captor.getValue().getAuthorities().iterator().next().getAuthority());
  }
}
