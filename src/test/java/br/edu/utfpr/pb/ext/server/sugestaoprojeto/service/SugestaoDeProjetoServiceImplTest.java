package br.edu.utfpr.pb.ext.server.sugestaoprojeto.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.event.EventPublisher;
import br.edu.utfpr.pb.ext.server.file.FileInfoDTO;
import br.edu.utfpr.pb.ext.server.file.FileService;
import br.edu.utfpr.pb.ext.server.file.img.ImageUtils;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.*;
import br.edu.utfpr.pb.ext.server.usuario.*;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SugestaoDeProjetoServiceImplTest {

  @Mock private SugestaoDeProjetoRepository repository;

  @Mock private UsuarioRepository usuarioRepository;

  @Mock private IUsuarioService usuarioService;

  @Mock private FileService fileService;

  @Mock private ImageUtils imageUtils;
  @Mock private EventPublisher eventPublisher;

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

  @ParameterizedTest
  @DisplayName("processarImagemUrl não deve processar quando imagemUrl for inválida")
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  void processarImagemUrl_quandoImagemUrlInvalida_naoDeveProcessar(String imagemUrl) {
    sugestao.setImagemUrl(imagemUrl);
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);
    sugestao.setProfessor(professor);
    when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.of(professor));

    service.preSave(sugestao);

    verify(imageUtils, never()).validateAndDecodeBase64Image(any());
    verify(fileService, never()).store(any(), any(), any());
    assertEquals(imagemUrl, sugestao.getImagemUrl());
  }

  @Test
  @DisplayName("processarImagemUrl não deve processar quando decodedImage for null")
  void processarImagemUrl_quandoDecodedImageNull_naoDeveProcessar() {
    String imagemUrl = "http://example.com/image.jpg";
    sugestao.setImagemUrl(imagemUrl);
    sugestao.setProfessor(professor);
    when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.of(professor));
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);
    when(imageUtils.validateAndDecodeBase64Image(imagemUrl)).thenReturn(null);

    service.preSave(sugestao);

    verify(imageUtils).validateAndDecodeBase64Image(imagemUrl);
    verify(fileService, never()).store(any(), any(), any());
    assertEquals(imagemUrl, sugestao.getImagemUrl());
  }

  @Test
  @DisplayName("processarImagemUrl deve processar e atualizar URL quando decodedImage for válida")
  void processarImagemUrl_quandoDecodedImageValida_deveProcessarEAtualizarUrl() {
    String base64Image = "data:image/png;base64,valid-base64-string";
    String finalUrl = "http://storage/sugestao-imagem.png";
    byte[] imageData = new byte[] {1, 2, 3};
    String contentType = "image/png";

    sugestao.setProfessor(professor);
    when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.of(professor));

    sugestao.setImagemUrl(base64Image);
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);

    ImageUtils.DecodedImage decodedImage = new ImageUtils.DecodedImage(imageData, contentType);
    when(imageUtils.validateAndDecodeBase64Image(base64Image)).thenReturn(decodedImage);

    FileInfoDTO fileInfoDTO = mock(FileInfoDTO.class);
    when(fileInfoDTO.getUrl()).thenReturn(finalUrl);
    when(fileService.store(imageData, contentType, "sugestao-imagem.png")).thenReturn(fileInfoDTO);

    try (var mockedStatic = mockStatic(ImageUtils.class)) {
      mockedStatic
          .when(() -> ImageUtils.getFileExtensionFromMimeType(contentType))
          .thenReturn("png");

      service.preSave(sugestao);

      assertEquals(finalUrl, sugestao.getImagemUrl());
      verify(imageUtils).validateAndDecodeBase64Image(base64Image);
      verify(fileService).store(imageData, contentType, "sugestao-imagem.png");
      mockedStatic.verify(() -> ImageUtils.getFileExtensionFromMimeType(contentType));
    }
  }

  @Test
  @DisplayName("processarImagemUrl deve lançar ResponseStatusException quando armazenamento falhar")
  void processarImagemUrl_quandoArmazenamentoFalhar_deveLancarResponseStatusException() {
    String base64Image = "data:image/png;base64,valid-base64-string";
    byte[] imageData = new byte[] {1, 2, 3};
    String contentType = "image/png";

    when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.of(professor));
    sugestao.setProfessor(professor);

    sugestao.setImagemUrl(base64Image);
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);

    ImageUtils.DecodedImage decodedImage = new ImageUtils.DecodedImage(imageData, contentType);
    when(imageUtils.validateAndDecodeBase64Image(base64Image)).thenReturn(decodedImage);
    when(fileService.store(any(), any(), any()))
        .thenThrow(new RuntimeException("Erro de armazenamento"));

    try (var mockedStatic = mockStatic(ImageUtils.class)) {
      mockedStatic
          .when(() -> ImageUtils.getFileExtensionFromMimeType(contentType))
          .thenReturn("png");

      ResponseStatusException exception =
          assertThrows(ResponseStatusException.class, () -> service.preSave(sugestao));

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
      assertEquals("Falha ao processar a imagem da sugestão de projeto.", exception.getReason());
      verify(imageUtils).validateAndDecodeBase64Image(base64Image);
      verify(fileService).store(imageData, contentType, "sugestao-imagem.png");
      mockedStatic.verify(() -> ImageUtils.getFileExtensionFromMimeType(contentType));
    }
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

  @Test
  @DisplayName(
      "listarIndicacoesDoUsuarioLogado deve retornar lista de indicações do usuário logado")
  void listarIndicacoesDoUsuarioLogado_DeveRetornarListaDeIndicacoesDoUsuarioLogado() {
    // Arrange
    when(usuarioService.obterUsuarioLogado()).thenReturn(professor);
    List<SugestaoDeProjeto> indicacoes = Collections.singletonList(sugestao);
    when(repository.findByProfessorId(professor.getId())).thenReturn(indicacoes);

    // Act
    List<SugestaoDeProjeto> result = service.listarIndicacoesDoUsuarioLogado();

    // Assert
    assertEquals(indicacoes, result);
    verify(usuarioService).obterUsuarioLogado();
    verify(repository).findByProfessorId(professor.getId());
  }

  @Test
  @DisplayName("postsave deve publicar evento SugestaoCriada para sugestões recém-criadas")
  void postsave_quandoSugestaoRecemCriada_devePublicarEventoSugestaoCriada() {
    // Arrange
    SugestaoDeProjeto sugestao = new SugestaoDeProjeto();
    sugestao.setDataCriacao(LocalDateTime.now().minusSeconds(5));

    // Act
    SugestaoDeProjeto result = service.postsave(sugestao);

    // Assert
    verify(eventPublisher).publishSugestaoCriada(sugestao);
    verify(eventPublisher, never()).publishSugestaoAtualizada(any());
    assertEquals(sugestao, result);
  }

  @Test
  @DisplayName("postsave deve publicar evento SugestaoAtualizada para sugestões existentes")
  void postsave_quandoSugestaoExistente_devePublicarEventoSugestaoAtualizada() {
    // Arrange
    SugestaoDeProjeto sugestao = new SugestaoDeProjeto();
    sugestao.setDataCriacao(LocalDateTime.now().minusMinutes(30));

    // Act
    SugestaoDeProjeto result = service.postsave(sugestao);

    // Assert
    verify(eventPublisher).publishSugestaoAtualizada(sugestao);
    verify(eventPublisher, never()).publishSugestaoCriada(any());
    assertEquals(sugestao, result);
  }

  @Test
  @DisplayName(
      "postsave deve publicar evento SugestaoAtualizada para sugestões sem data de criação")
  void postsave_quandoSugestaoSemDataCriacao_devePublicarEventoSugestaoAtualizada() {
    // Arrange
    SugestaoDeProjeto sugestao = new SugestaoDeProjeto();
    sugestao.setDataCriacao(null);

    // Act
    SugestaoDeProjeto result = service.postsave(sugestao);

    // Assert
    verify(eventPublisher).publishSugestaoAtualizada(sugestao);
    verify(eventPublisher, never()).publishSugestaoCriada(any());
    assertEquals(sugestao, result);
  }
}
