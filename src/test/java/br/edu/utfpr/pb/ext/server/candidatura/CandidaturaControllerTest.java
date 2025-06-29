package br.edu.utfpr.pb.ext.server.candidatura;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

class CandidaturaControllerTest {

  private ICandidaturaService candidaturaService;
  private UsuarioRepository usuarioRepository;
  private CandidaturaController candidaturaController;

  @BeforeEach
  void setUp() {
    candidaturaService = mock(ICandidaturaService.class);
    usuarioRepository = mock(UsuarioRepository.class);
    candidaturaController = new CandidaturaController(candidaturaService, usuarioRepository);
  }

  @Test
  void candidatar_quandoUsuarioExiste_entaoRetornaCandidaturaDTO() {
    Long projetoId = 1L;
    String email = "aluno@teste.com";

    Usuario aluno = new Usuario();
    aluno.setId(10L);
    aluno.setEmail(email);

    CandidaturaDTO candidaturaDTO = new CandidaturaDTO();
    candidaturaDTO.setId(100L);

    Authentication authentication = mock(Authentication.class);
    when(authentication.getName()).thenReturn(email);
    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(aluno));
    when(candidaturaService.candidatar(projetoId, aluno.getId())).thenReturn(candidaturaDTO);

    ResponseEntity<CandidaturaDTO> response =
        candidaturaController.candidatar(projetoId, authentication);

    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(100L, response.getBody().getId());
  }

  @Test
  void candidatar_quandoUsuarioNaoExiste_entaoLancaExcecao() {
    Long projetoId = 1L;
    String email = "naoexiste@teste.com";

    Authentication authentication = mock(Authentication.class);
    when(authentication.getName()).thenReturn(email);
    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () -> candidaturaController.candidatar(projetoId, authentication));

    assertEquals("Usuário autenticado não encontrado", ex.getMessage());
  }
}
