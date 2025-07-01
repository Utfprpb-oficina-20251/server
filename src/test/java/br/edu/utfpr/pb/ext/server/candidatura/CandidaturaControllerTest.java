package br.edu.utfpr.pb.ext.server.candidatura;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

class CandidaturaControllerTest {

  private ICandidaturaService candidaturaService;
  private CandidaturaController candidaturaController;
  private ModelMapper modelMapper;
  private Usuario usuarioMock;

  @BeforeEach
  void setUp() {
    candidaturaService = mock(ICandidaturaService.class);
    modelMapper = new ModelMapper();
    candidaturaController = new CandidaturaController(candidaturaService, modelMapper);

    usuarioMock = new Usuario();
    usuarioMock.setId(1L);
    usuarioMock.setNome("Teste Usuario");
  }

  @Test
  void candidatar_quandoNovaCandidatura_entaoRetornaCandidaturaDTO() {
    // Arrange
    Long projetoId = 1L;

    Candidatura candidatura = new Candidatura();
    candidatura.setId(100L);
    candidatura.setStatus(StatusCandidatura.PENDENTE);

    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    candidatura.setProjeto(projeto);
    candidatura.setAluno(usuarioMock);

    when(candidaturaService.candidatar(projetoId)).thenReturn(candidatura);

    // Act
    ResponseEntity<CandidaturaDTO> response = candidaturaController.candidatar(projetoId);

    // Assert
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(100L, response.getBody().getId());
    verify(candidaturaService, times(1)).candidatar(projetoId);
  }

  @Test
  void candidatar_quandoCandidaturaPendente_entaoLancaExcecao() {
    // Arrange
    Long projetoId = 1L;
    when(candidaturaService.candidatar(projetoId))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Você já está inscrito neste projeto"));

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> candidaturaController.candidatar(projetoId));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Você já está inscrito neste projeto", exception.getReason());
    verify(candidaturaService, times(1)).candidatar(projetoId);
  }

  @Test
  void candidatar_quandoCandidaturaAprovada_entaoLancaExcecao() {
    // Arrange
    Long projetoId = 1L;
    when(candidaturaService.candidatar(projetoId))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Sua candidatura já foi aprovada para este projeto"));

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> candidaturaController.candidatar(projetoId));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Sua candidatura já foi aprovada para este projeto", exception.getReason());
  }

  @Test
  void candidatar_quandoCandidaturaRejeitada_entaoLancaExcecao() {
    // Arrange
    Long projetoId = 1L;
    when(candidaturaService.candidatar(projetoId))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Sua candidatura foi rejeitada para este projeto"));

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> candidaturaController.candidatar(projetoId));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Sua candidatura foi rejeitada para este projeto", exception.getReason());
  }

  @Test
  void candidatar_quandoCandidaturaCancelada_entaoAtualizaParaPendente() {
    // Arrange
    Long projetoId = 1L;

    Candidatura candidatura = new Candidatura();
    candidatura.setId(100L);
    candidatura.setStatus(StatusCandidatura.PENDENTE);

    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    candidatura.setProjeto(projeto);
    candidatura.setAluno(usuarioMock);

    when(candidaturaService.candidatar(projetoId)).thenReturn(candidatura);

    // Act
    ResponseEntity<CandidaturaDTO> response = candidaturaController.candidatar(projetoId);

    // Assert
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(StatusCandidatura.PENDENTE, response.getBody().getStatusCandidatura());
    verify(candidaturaService, times(1)).candidatar(projetoId);
  }

  @Test
  void listarMinhasCandidaturas_quandoExistemCandidaturas_entaoRetornaLista() {
    // Arrange
    List<Candidatura> candidaturas =
        Arrays.asList(
            createCandidatura(1L, StatusCandidatura.PENDENTE),
            createCandidatura(2L, StatusCandidatura.APROVADA));

    when(candidaturaService.findAllByAlunoId(usuarioMock.getId())).thenReturn(candidaturas);

    // Act
    ResponseEntity<List<CandidaturaDTO>> response =
        candidaturaController.listarMinhasCandidaturas(usuarioMock);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2, response.getBody().size());
    verify(candidaturaService).findAllByAlunoId(usuarioMock.getId());
  }

  @Test
  void listarMinhasCandidaturas_quandoNaoExistemCandidaturas_entaoRetornaListaVazia() {
    // Arrange
    when(candidaturaService.findAllByAlunoId(usuarioMock.getId()))
        .thenReturn(Collections.emptyList());

    // Act
    ResponseEntity<List<CandidaturaDTO>> response =
        candidaturaController.listarMinhasCandidaturas(usuarioMock);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().isEmpty());
  }

  @Test
  void listarCandidaturasPorProjeto_quandoExistemCandidaturas_entaoRetornaLista() {
    // Arrange
    Long projetoId = 1L;
    List<Candidatura> candidaturas =
        Arrays.asList(
            createCandidatura(1L, StatusCandidatura.PENDENTE),
            createCandidatura(2L, StatusCandidatura.PENDENTE));

    when(candidaturaService.findAllPendentesByProjetoId(projetoId)).thenReturn(candidaturas);

    // Act
    ResponseEntity<List<CandidaturaDTO>> response =
        candidaturaController.listarCandidaturasPorProjeto(projetoId);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2, response.getBody().size());
  }

  @Test
  void listarCandidaturasPorProjeto_quandoNaoExistemCandidaturas_entaoRetornaListaVazia() {
    // Arrange
    Long projetoId = 1L;
    when(candidaturaService.findAllPendentesByProjetoId(projetoId))
        .thenReturn(Collections.emptyList());

    // Act
    ResponseEntity<List<CandidaturaDTO>> response =
        candidaturaController.listarCandidaturasPorProjeto(projetoId);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().isEmpty());
  }

  @Test
  void atualizarStatusCandidaturas_quandoProjetoIdValido_entaoAtualizaComSucesso() {
    // Arrange
    Long projetoId = 1L;

    List<CandidaturaDTO> candidaturaDTOs =
        Arrays.asList(
            createCandidaturaDTO(1L, projetoId, StatusCandidatura.APROVADA),
            createCandidaturaDTO(2L, projetoId, StatusCandidatura.REJEITADA));

    // Act
    ResponseEntity<String> response =
        candidaturaController.atualizarStatusCandidaturas(projetoId, candidaturaDTOs);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(candidaturaService).atualizarStatusCandidaturas(anyList());
  }

  @Test
  void atualizarStatusCandidaturas_quandoProjetoIdInvalido_entaoRetornaUnprocessableEntity() {
    // Arrange
    Long projetoId = 1L;
    Long diferenteProjetoId = 2L;

    List<CandidaturaDTO> candidaturaDTOs =
        List.of(createCandidaturaDTO(1L, diferenteProjetoId, StatusCandidatura.APROVADA));

    // Act
    ResponseEntity<String> response =
        candidaturaController.atualizarStatusCandidaturas(projetoId, candidaturaDTOs);

    // Assert
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertEquals(
        "O ID do projeto na candidatura não corresponde ao projeto informado na URL",
        response.getBody());
    verify(candidaturaService, never()).atualizarStatusCandidaturas(anyList());
  }

  @Test
  void cancelarCandidatura_quandoCandidaturaExisteEPertenceAoUsuario_entaoCancelaComSucesso() {
    // Arrange
    Long candidaturaId = 1L;

    Candidatura candidatura = new Candidatura();
    candidatura.setId(candidaturaId);
    candidatura.setAluno(usuarioMock);

    when(candidaturaService.findById(candidaturaId)).thenReturn(candidatura);

    // Act
    ResponseEntity<String> response =
        candidaturaController.cancelarCandidatura(candidaturaId, usuarioMock);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Candidatura cancelada com sucesso", response.getBody());
    assertEquals(StatusCandidatura.CANCELADA, candidatura.getStatus());
    verify(candidaturaService).atualizarStatusCandidaturas(anyList());
  }

  @Test
  void cancelarCandidatura_quandoCandidaturaNaoExiste_entaoRetornaNotFound() {
    // Arrange
    Long candidaturaId = 1L;
    when(candidaturaService.findById(candidaturaId)).thenReturn(null);

    // Act
    ResponseEntity<String> response =
        candidaturaController.cancelarCandidatura(candidaturaId, usuarioMock);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("Candidatura com ID " + candidaturaId + " não encontrada", response.getBody());
    verify(candidaturaService, never()).atualizarStatusCandidaturas(anyList());
  }

  @Test
  void cancelarCandidatura_quandoCandidaturaNaoPertenceAoUsuario_entaoRetornaForbidden() {
    // Arrange
    Long candidaturaId = 1L;

    Usuario outroUsuario = new Usuario();
    outroUsuario.setId(2L);

    Candidatura candidatura = new Candidatura();
    candidatura.setId(candidaturaId);
    candidatura.setAluno(outroUsuario);

    when(candidaturaService.findById(candidaturaId)).thenReturn(candidatura);

    // Act
    ResponseEntity<String> response =
        candidaturaController.cancelarCandidatura(candidaturaId, usuarioMock);

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals("Você não tem permissão para cancelar esta candidatura", response.getBody());
    verify(candidaturaService, never()).atualizarStatusCandidaturas(anyList());
  }

  // Helper methods
  private Candidatura createCandidatura(Long id, StatusCandidatura status) {
    Candidatura candidatura = new Candidatura();
    candidatura.setId(id);
    candidatura.setStatus(status);
    candidatura.setAluno(usuarioMock);

    Projeto projeto = new Projeto();
    projeto.setId(1L);
    candidatura.setProjeto(projeto);

    return candidatura;
  }

  private CandidaturaDTO createCandidaturaDTO(Long id, Long projetoId, StatusCandidatura status) {
    CandidaturaDTO dto = new CandidaturaDTO();
    dto.setId(id);
    dto.setProjetoId(projetoId);
    dto.setStatusCandidatura(status);
    return dto;
  }
}
