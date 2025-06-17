package br.edu.utfpr.pb.ext.server.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationProvider;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock UsuarioRepository usuarioRepository;
  @Mock AuthorityRepository authorityRepository;
  @Mock EmailOtpAuthenticationProvider emailOtpAuthenticationProvider;
  @InjectMocks AuthService authService;

  @Test
  @DisplayName("Solicitar código OTP quando ocorrer erro deve lançar ResponseStatusException")
  void solicitarCodigoOtp_whenErroOcorrer_deveRetornarResponseStatusException() {
    String email = "testuser@alunos.utfpr.edu.br";
    when(usuarioRepository.findByEmail(email)).thenThrow(new RuntimeException());
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> authService.solicitarCodigoOtp(email));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    verify(usuarioRepository, times(1)).findByEmail(email);
  }

  @Test
  @DisplayName("Cadastro de usuário com domínio @utfpr.edu.br deve receber ROLE_SERVIDOR")
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

  @Test
  @DisplayName("Cadastro de usuário com domínio @alunos.utfpr.edu.br deve receber ROLE_ALUNO")
  void cadastro_whenUsuarioTemAlunoNoEmail_deveRetornarUsuarioComRoleAluno() {
    CadastroUsuarioDTO cadastroDTO =
        CadastroUsuarioDTO.builder()
            .nome("testuser")
            .email("testuser@alunos.utfpr.edu.br")
            .registro("12345678901")
            .build();
    when(authorityRepository.findByAuthority("ROLE_ALUNO"))
        .thenReturn(Optional.of(Authority.builder().authority("ROLE_ALUNO").build()));
    authService.cadastro(cadastroDTO);

    ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
    verify(usuarioRepository, times(1)).save(captor.capture());

    assertEquals(1, captor.getValue().getAuthorities().size());
    assertEquals("ROLE_ALUNO", captor.getValue().getAuthorities().iterator().next().getAuthority());
  }

  @Test
  @DisplayName("Cadastro de usuário que ja existe deve retornar ResponseStatusException")
  void cadastro_whenUsuarioJaExiste_deveRetornar409() {
    CadastroUsuarioDTO cadastroDTO =
        CadastroUsuarioDTO.builder()
            .nome("testuser")
            .email("testuser@alunos.utfpr.edu.br")
            .registro("12345678901")
            .build();
    when(usuarioRepository.findByEmail(cadastroDTO.getEmail()))
        .thenReturn(Optional.of(new Usuario()));
    assertThrows(ResponseStatusException.class, () -> authService.cadastro(cadastroDTO));
  }
}
