package br.edu.utfpr.pb.ext.server.usuario;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

  @Mock private SecurityContext securityContext;

  @Mock private Authentication authentication;

  @Mock private Usuario usuario;

  @InjectMocks private UsuarioServiceImpl usuarioService;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void obterUsuarioLogado_QuandoAuthenticationNull_DeveLancarIllegalStateException() {
    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
      mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(null);

      IllegalStateException ex =
          assertThrows(IllegalStateException.class, () -> usuarioService.obterUsuarioLogado());
      assertEquals("Nenhum usuário autenticado!", ex.getMessage());
    }
  }

  @Test
  void obterUsuarioLogado_QuandoUsuarioNaoAutenticado_DeveLancarIllegalStateException() {
    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
      mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(false);

      IllegalStateException ex =
          assertThrows(IllegalStateException.class, () -> usuarioService.obterUsuarioLogado());
      assertEquals("Nenhum usuário autenticado!", ex.getMessage());
    }
  }

  @Test
  void obterUsuarioLogado_QuandoPrincipalNaoEUsuario_DeveLancarIllegalStateException() {
    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
      mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(true);
      when(authentication.getPrincipal()).thenReturn("not-a-usuario-object");

      IllegalStateException ex =
          assertThrows(IllegalStateException.class, () -> usuarioService.obterUsuarioLogado());
      assertEquals("Principal não é uma instância de Usuario!", ex.getMessage());
    }
  }

  @Test
  void obterUsuarioLogado_QuandoDadosValidos_DeveRetornarUsuario() {
    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
      mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(true);
      when(authentication.getPrincipal()).thenReturn(usuario);

      Usuario resultado = usuarioService.obterUsuarioLogado();
      assertSame(usuario, resultado);
    }
  }

  @Test
  void validarProfessor_QuandoProfessorInativo_DeveLancarIllegalArgumentException() {
    when(usuario.isAtivo()).thenReturn(false);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> usuarioService.validarProfessor(usuario));
    assertEquals("Professor deve estar ativo", ex.getMessage());
  }

  @Test
  void validarProfessor_QuandoProfessorSemRoleServidor_DeveLancarIllegalArgumentException() {
    when(usuario.isAtivo()).thenReturn(true);
    Set auths =
        Set.of(new SimpleGrantedAuthority("ROLE_ALUNO"), new SimpleGrantedAuthority("ROLE_OTHER"));
    when(usuario.getAuthorities()).thenReturn(auths);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> usuarioService.validarProfessor(usuario));
    assertEquals("Professor deve ter perfil de servidor", ex.getMessage());
  }

  @Test
  void validarProfessor_QuandoProfessorValido_NaoDeveLancarExcecao() {
    when(usuario.isAtivo()).thenReturn(true);
    Set auths =
        Set.of(
            new SimpleGrantedAuthority("ROLE_SERVIDOR"), new SimpleGrantedAuthority("ROLE_OTHER"));
    when(usuario.getAuthorities()).thenReturn(auths);

    assertDoesNotThrow(() -> usuarioService.validarProfessor(usuario));
  }

  @Test
  void validarProfessor_QuandoProfessorComApenasRoleServidor_NaoDeveLancarExcecao() {
    when(usuario.isAtivo()).thenReturn(true);
    Set auths = Set.of(new SimpleGrantedAuthority("ROLE_SERVIDOR"));
    when(usuario.getAuthorities()).thenReturn(auths);

    assertDoesNotThrow(() -> usuarioService.validarProfessor(usuario));
  }
}
