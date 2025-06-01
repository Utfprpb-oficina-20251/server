package br.edu.utfpr.pb.ext.server.sugestaoprojeto.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.sugestaoprojeto.*;
import br.edu.utfpr.pb.ext.server.usuario.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

@ExtendWith(MockitoExtension.class)
class SugestaoDeProjetoServiceImplTest {

  @Mock private SugestaoDeProjetoRepository repository;

  @Mock private UsuarioRepository usuarioRepository;

  @Mock private IUsuarioService usuarioService;

  @InjectMocks private SugestaoDeProjetoServiceImpl service;

  private Usuario aluno;
  private Usuario professor;
  private SugestaoDeProjeto sugestao;

  @BeforeEach
  void setUp() {
    aluno = new Usuario();
    aluno.setId(1L);
    aluno.setNome("Aluno Teste");

    professor = new Usuario();
    professor.setId(2L);
    professor.setNome("Professor Teste");

    sugestao = new SugestaoDeProjeto();
    sugestao.setId(1L);
    sugestao.setTitulo("Título da Sugestão");
    sugestao.setDescricao("Descrição detalhada da sugestão de projeto");
    sugestao.setPublicoAlvo("Estudantes de graduação");
    sugestao.setAluno(aluno);
    sugestao.setProfessor(professor);
    sugestao.setStatus(StatusSugestao.AGUARDANDO);
  }

  @Test
  @DisplayName("getRepository deve retornar o repositório correto")
  void getRepository_DeveRetornarRepositorioCorreto() {
    JpaRepository<SugestaoDeProjeto, Long> result = service.getRepository();
    assertEquals(repository, result);
  }

  @Test
  @DisplayName("preSave deve configurar corretamente a sugestão quando professor é válido")
  void preSave_QuandoProfessorValido_DeveConfigurarSugestaoCorretamente() {
    // Arrange
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);
    when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.of(professor));
    doNothing().when(usuarioService).validarProfessor(professor);

    // Act
    service.preSave(sugestao);

    // Assert
    verify(usuarioService).obterUsuarioLogado();
    verify(usuarioRepository).findById(professor.getId());
    verify(usuarioService).validarProfessor(professor);

    assertEquals(professor, sugestao.getProfessor());
    assertEquals(StatusSugestao.AGUARDANDO, sugestao.getStatus());
    assertEquals(aluno, sugestao.getAluno());
  }

  @Test
  @DisplayName("preSave deve lançar EntityNotFoundException quando professor não é encontrado")
  void preSave_QuandoProfessorNaoEncontrado_DeveLancarEntityNotFoundException() {
    // Arrange
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);
    when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.empty());

    // Act & Assert
    EntityNotFoundException exception =
        assertThrows(EntityNotFoundException.class, () -> service.preSave(sugestao));

    assertEquals("Professor não encontrado", exception.getMessage());
    verify(usuarioService).obterUsuarioLogado();
    verify(usuarioRepository).findById(professor.getId());
    verify(usuarioService, never()).validarProfessor(any());
  }

  @Test
  @DisplayName("preSave deve configurar sugestão sem professor quando professor.id é null")
  void preSave_QuandoProfessorIdNull_DeveConfigurarSugestaoSemProfessor() {
    // Arrange
    professor.setId(null);
    sugestao.setProfessor(professor);
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);

    // Act
    service.preSave(sugestao);

    // Assert
    verify(usuarioService).obterUsuarioLogado();
    verify(usuarioRepository, never()).findById(any());
    verify(usuarioService, never()).validarProfessor(any());
  }

  @Test
  @DisplayName("listarPorAluno deve retornar lista de sugestões do aluno")
  void listarPorAluno_DeveRetornarListaDeSugestoesDoAluno() {
    // Arrange
    Long alunoId = 1L;
    List<SugestaoDeProjeto> sugestoes = Collections.singletonList(sugestao);
    when(repository.findByAlunoId(alunoId)).thenReturn(sugestoes);

    // Act
    List<SugestaoDeProjeto> result = service.listarPorAluno(alunoId);

    // Assert
    assertEquals(sugestoes, result);
    verify(repository).findByAlunoId(alunoId);
  }

  @Test
  @DisplayName("listarSugestoesDoUsuarioLogado deve retornar lista de sugestões do usuário logado")
  void listarSugestoesDoUsuarioLogado_DeveRetornarListaDeSugestoesDoUsuarioLogado() {
    // Arrange
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);
    List<SugestaoDeProjeto> sugestoes = Collections.singletonList(sugestao);
    when(repository.findByAlunoId(aluno.getId())).thenReturn(sugestoes);

    // Act
    List<SugestaoDeProjeto> result = service.listarSugestoesDoUsuarioLogado();

    // Assert
    assertEquals(sugestoes, result);
    verify(usuarioService).obterUsuarioLogado();
    verify(repository).findByAlunoId(aluno.getId());
  }
}
