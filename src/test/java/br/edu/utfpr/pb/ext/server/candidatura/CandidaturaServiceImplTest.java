package br.edu.utfpr.pb.ext.server.candidatura;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.projeto.IProjetoService;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.IUsuarioService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class CandidaturaServiceImplTest {

  private CandidaturaRepository candidaturaRepository;
  private IUsuarioService usuarioService;
  private IProjetoService projetoService;
  private CandidaturaServiceImpl candidaturaService;

  private Usuario aluno;
  private Projeto projeto;

  @BeforeEach
  void setUp() {
    candidaturaRepository = mock(CandidaturaRepository.class);
    usuarioService = mock(IUsuarioService.class);
    projetoService = mock(IProjetoService.class);

    candidaturaService =
        new CandidaturaServiceImpl(candidaturaRepository, usuarioService, projetoService);

    aluno = Usuario.builder().nome("Aluno Teste").build();
    aluno.setId(1L);

    projeto = Projeto.builder().status(StatusProjeto.EM_ANDAMENTO).qtdeVagas(5L).build();
    projeto.setId(1L);
  }

  @Test
  void candidatar_quandoNovaCandidatura_entaoSucesso() {
    // Arrange
    when(projetoService.findOne(1L)).thenReturn(projeto);
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);
    when(candidaturaRepository.findByProjetoIdAndAlunoId(1L, 1L)).thenReturn(Optional.empty());

    Candidatura candidaturaSalva = Candidatura.builder().projeto(projeto).aluno(aluno).build();
    candidaturaSalva.setId(1L);

    when(candidaturaRepository.save(any(Candidatura.class))).thenReturn(candidaturaSalva);

    // Act
    Candidatura resultado = candidaturaService.candidatar(1L);

    // Assert
    assertNotNull(resultado);
    assertEquals(1L, resultado.getId());
    verify(candidaturaRepository).save(any(Candidatura.class));
  }

  @Test
  void candidatar_quandoProjetoNaoEmAndamento_entaoLancaExcecao() {
    // Arrange
    projeto.setStatus(StatusProjeto.CANCELADO);
    when(projetoService.findOne(1L)).thenReturn(projeto);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> candidaturaService.candidatar(1L));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Projeto não está aberto para candidaturas", exception.getReason());
  }

  @Test
  void candidatar_quandoCandidaturaPendente_entaoLancaExcecao() {
    // Arrange
    Candidatura candidaturaExistente = createCandidatura(StatusCandidatura.PENDENTE);

    when(projetoService.findOne(1L)).thenReturn(projeto);
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);
    when(candidaturaRepository.findByProjetoIdAndAlunoId(1L, 1L))
        .thenReturn(Optional.of(candidaturaExistente));

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> candidaturaService.candidatar(1L));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Você já está inscrito neste projeto", exception.getReason());
  }

  @Test
  void candidatar_quandoCandidaturaAprovada_entaoLancaExcecao() {
    // Arrange
    Candidatura candidaturaExistente = createCandidatura(StatusCandidatura.APROVADA);

    when(projetoService.findOne(1L)).thenReturn(projeto);
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);
    when(candidaturaRepository.findByProjetoIdAndAlunoId(1L, 1L))
        .thenReturn(Optional.of(candidaturaExistente));

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> candidaturaService.candidatar(1L));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Sua candidatura já foi aprovada para este projeto", exception.getReason());
  }

  @Test
  void candidatar_quandoCandidaturaRejeitada_entaoLancaExcecao() {
    // Arrange
    Candidatura candidaturaExistente = createCandidatura(StatusCandidatura.REJEITADA);

    when(projetoService.findOne(1L)).thenReturn(projeto);
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);
    when(candidaturaRepository.findByProjetoIdAndAlunoId(1L, 1L))
        .thenReturn(Optional.of(candidaturaExistente));

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> candidaturaService.candidatar(1L));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Sua candidatura foi rejeitada para este projeto", exception.getReason());
  }

  @Test
  void candidatar_quandoCandidaturaCancelada_entaoAtualizaParaPendente() {
    // Arrange
    Candidatura candidaturaExistente = createCandidatura(StatusCandidatura.CANCELADA);
    LocalDateTime dataAnterior = candidaturaExistente.getDataCandidatura();

    when(projetoService.findOne(1L)).thenReturn(projeto);
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);
    when(candidaturaRepository.findByProjetoIdAndAlunoId(1L, 1L))
        .thenReturn(Optional.of(candidaturaExistente));
    when(candidaturaRepository.save(candidaturaExistente)).thenReturn(candidaturaExistente);

    // Act
    Candidatura resultado = candidaturaService.candidatar(1L);

    // Assert
    assertEquals(StatusCandidatura.PENDENTE, resultado.getStatus());
    assertNotEquals(dataAnterior, resultado.getDataCandidatura());
    verify(candidaturaRepository).save(candidaturaExistente);
  }

  @Test
  void atualizarStatusCandidaturas_quandoListaVazia_entaoLancaExcecao() {
    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> candidaturaService.atualizarStatusCandidaturas(Collections.emptyList()));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Lista de candidaturas vazia", exception.getReason());
  }

  @Test
  void atualizarStatusCandidaturas_quandoListaNula_entaoLancaExcecao() {
    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> candidaturaService.atualizarStatusCandidaturas(null));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Lista de candidaturas vazia", exception.getReason());
  }

  @Test
  void atualizarStatusCandidaturas_quandoCandidaturaNaoExiste_entaoLancaExcecao() {
    // Arrange
    Candidatura candidatura = createCandidatura(StatusCandidatura.APROVADA);
    candidatura.setId(999L);

    when(candidaturaRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> candidaturaService.atualizarStatusCandidaturas(List.of(candidatura)));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Candidatura com ID 999 não encontrada", exception.getReason());
  }

  @Test
  void atualizarStatusCandidaturas_quandoCandidaturaJaAprovada_entaoLancaExcecao() {
    // Arrange
    Candidatura candidatura = createCandidatura(StatusCandidatura.REJEITADA);
    candidatura.setId(1L);

    Candidatura candidaturaBD = createCandidatura(StatusCandidatura.APROVADA);
    candidaturaBD.setId(1L);

    when(candidaturaRepository.findById(1L)).thenReturn(Optional.of(candidaturaBD));

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> candidaturaService.atualizarStatusCandidaturas(List.of(candidatura)));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Candidatura 1 já foi aprovada", exception.getReason());
  }

  @Test
  void atualizarStatusCandidaturas_quandoCandidaturaJaRejeitada_entaoLancaExcecao() {
    // Arrange
    Candidatura candidatura = createCandidatura(StatusCandidatura.APROVADA);
    candidatura.setId(1L);

    Candidatura candidaturaBD = createCandidatura(StatusCandidatura.REJEITADA);
    candidaturaBD.setId(1L);

    when(candidaturaRepository.findById(1L)).thenReturn(Optional.of(candidaturaBD));

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> candidaturaService.atualizarStatusCandidaturas(List.of(candidatura)));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Candidatura 1 já foi rejeitada", exception.getReason());
  }

  @Test
  void atualizarStatusCandidaturas_quandoValida_entaoAtualizaComSucesso() {
    // Arrange
    Candidatura candidatura1 = createCandidatura(StatusCandidatura.APROVADA);
    candidatura1.setId(1L);

    Candidatura candidatura2 = createCandidatura(StatusCandidatura.REJEITADA);
    candidatura2.setId(2L);

    Candidatura candidaturaBD1 = createCandidatura(StatusCandidatura.PENDENTE);
    candidaturaBD1.setId(1L);

    Candidatura candidaturaBD2 = createCandidatura(StatusCandidatura.PENDENTE);
    candidaturaBD2.setId(2L);

    when(candidaturaRepository.findById(1L)).thenReturn(Optional.of(candidaturaBD1));
    when(candidaturaRepository.findById(2L)).thenReturn(Optional.of(candidaturaBD2));
    when(candidaturaRepository.save(any())).thenReturn(candidaturaBD1, candidaturaBD2);

    // Act
    candidaturaService.atualizarStatusCandidaturas(Arrays.asList(candidatura1, candidatura2));

    // Assert
    assertEquals(StatusCandidatura.APROVADA, candidaturaBD1.getStatus());
    assertEquals(StatusCandidatura.REJEITADA, candidaturaBD2.getStatus());
    verify(candidaturaRepository, times(2)).save(any(Candidatura.class));
  }

  @Test
  void findAllByAlunoId_retornaListaVazia_quandoNaoEncontrado() {
    // Arrange
    when(candidaturaRepository.findAllByAlunoId(1L)).thenReturn(Optional.empty());

    // Act
    List<Candidatura> resultado = candidaturaService.findAllByAlunoId(1L);

    // Assert
    assertTrue(resultado.isEmpty());
    verify(candidaturaRepository).findAllByAlunoId(1L);
  }

  @Test
  void findAllByAlunoId_retornaLista_quandoEncontrado() {
    // Arrange
    List<Candidatura> candidaturas =
        Arrays.asList(
            createCandidatura(StatusCandidatura.PENDENTE),
            createCandidatura(StatusCandidatura.APROVADA));

    when(candidaturaRepository.findAllByAlunoId(1L)).thenReturn(Optional.of(candidaturas));

    // Act
    List<Candidatura> resultado = candidaturaService.findAllByAlunoId(1L);

    // Assert
    assertEquals(2, resultado.size());
    assertEquals(candidaturas, resultado);
  }

  @Test
  void findAllPendentesByProjetoId_retornaListaVazia_quandoNaoEncontrado() {
    // Arrange
    when(candidaturaRepository.findAllByProjetoIdAndStatus(1L, StatusCandidatura.PENDENTE))
        .thenReturn(Optional.empty());

    // Act
    List<Candidatura> resultado = candidaturaService.findAllPendentesByProjetoId(1L);

    // Assert
    assertTrue(resultado.isEmpty());
    verify(candidaturaRepository).findAllByProjetoIdAndStatus(1L, StatusCandidatura.PENDENTE);
  }

  @Test
  void findAllPendentesByProjetoId_retornaLista_quandoEncontrado() {
    // Arrange
    List<Candidatura> candidaturas =
        Arrays.asList(
            createCandidatura(StatusCandidatura.PENDENTE),
            createCandidatura(StatusCandidatura.PENDENTE));

    when(candidaturaRepository.findAllByProjetoIdAndStatus(1L, StatusCandidatura.PENDENTE))
        .thenReturn(Optional.of(candidaturas));

    // Act
    List<Candidatura> resultado = candidaturaService.findAllPendentesByProjetoId(1L);

    // Assert
    assertEquals(2, resultado.size());
    assertEquals(candidaturas, resultado);
  }

  @Test
  void findById_quandoNaoEncontrado_entaoLancaExcecao() {
    // Arrange
    when(candidaturaRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> candidaturaService.findById(1L));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Candidatura com ID 1 não encontrada", exception.getReason());
  }

  @Test
  void findById_quandoEncontrado_entaoRetornaCandidatura() {
    // Arrange
    Candidatura candidatura = createCandidatura(StatusCandidatura.PENDENTE);
    candidatura.setId(1L);

    when(candidaturaRepository.findById(1L)).thenReturn(Optional.of(candidatura));

    // Act
    Candidatura resultado = candidaturaService.findById(1L);

    // Assert
    assertEquals(candidatura, resultado);
    assertEquals(1L, resultado.getId());
  }

  // Helper method
  private Candidatura createCandidatura(StatusCandidatura status) {
    return Candidatura.builder()
        .projeto(projeto)
        .aluno(aluno)
        .status(status)
        .dataCandidatura(LocalDateTime.now().minusDays(1))
        .build();
  }
}
