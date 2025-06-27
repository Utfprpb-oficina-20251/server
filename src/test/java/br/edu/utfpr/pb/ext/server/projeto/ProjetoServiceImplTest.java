package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.event.EventPublisher;
import br.edu.utfpr.pb.ext.server.file.FileInfoDTO;
import br.edu.utfpr.pb.ext.server.file.FileService;
import br.edu.utfpr.pb.ext.server.file.img.ImageUtils;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.jetbrains.annotations.NotNull;
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
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceImplTest {

  @InjectMocks private ProjetoServiceImpl projetoService;

  @Mock private ProjetoRepository projetoRepository;
  @Mock private ModelMapper modelMapper;
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private FileService fileService;
  @Mock private ImageUtils imageUtils;
  @Mock private EventPublisher eventPublisher;
  private final Long projetoId = 1L;
  private final Long servidorId = 100L;
  private final Long alunoId = 200L;

  private Usuario servidor;
  private Usuario aluno;

  private Projeto projetoMock;
  private ProjetoDTO projetoDTOMock;

  @BeforeEach
  void setUp() {
    servidor = new Usuario();
    servidor.setId(servidorId);
    servidor.setSiape("1234567");

    aluno = new Usuario();
    aluno.setId(alunoId);
    aluno.setRegistroAcademico("20230123");

    projetoMock = new Projeto();
    projetoMock.setId(1L);
    projetoMock.setTitulo("Projeto Teste");

    projetoDTOMock = new ProjetoDTO();
    projetoDTOMock.setId(1L);
    projetoDTOMock.setTitulo("Projeto Teste");
  }

  // Teste feliz: servidor autorizado cancela o projeto
  @Test
  void deveCancelarProjetoQuandoServidorDaEquipe() {
    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
    projeto.setEquipeExecutora(Collections.singletonList(servidor));

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa válida");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    projetoService.cancelar(projetoId, dto, servidorId);

    assertEquals(StatusProjeto.CANCELADO, projeto.getStatus());
    assertEquals("Justificativa válida", projeto.getJustificativaCancelamento());
    verify(projetoRepository).save(projeto);
  }

  // Justificativa nula deve lançar exceção 400
  @Test
  void deveLancarExcecaoSeJustificativaForNula() {
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa(null);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, servidorId));

    assertEquals(400, ex.getStatusCode().value());
  }

  // Justificativa vazia deve lançar exceção 400
  @Test
  void deveLancarExcecaoSeJustificativaForVazia() {
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("   ");

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, servidorId));

    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void atualizarProjeto_quandoProjetoExiste_deveRetornarDTOAtualizado() {
    ProjetoDTO dadosParaAtualizar = new ProjetoDTO();
    dadosParaAtualizar.setTitulo("Novo Título do Projeto");
    dadosParaAtualizar.setDescricao("Nova descrição.");

    Projeto projetoOriginal = new Projeto();
    projetoOriginal.setId(projetoId);
    projetoOriginal.setTitulo("Título Antigo");
    projetoOriginal.setDescricao("Descrição antiga.");

    ProjetoDTO dtoEsperado = new ProjetoDTO();
    dtoEsperado.setId(projetoId);
    dtoEsperado.setTitulo("Novo Título do Projeto");
    dtoEsperado.setDescricao("Nova descrição.");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projetoOriginal));
    doNothing().when(modelMapper).map(any(ProjetoDTO.class), any(Projeto.class));
    when(projetoRepository.save(any(Projeto.class))).thenReturn(projetoOriginal);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(dtoEsperado);

    ProjetoDTO resultado = projetoService.atualizarProjeto(projetoId, dadosParaAtualizar);

    assertNotNull(resultado);
    assertEquals(dtoEsperado.getId(), resultado.getId());
    assertEquals(dtoEsperado.getTitulo(), resultado.getTitulo());

    verify(projetoRepository).findById(projetoId);
    verify(modelMapper).map(dadosParaAtualizar, projetoOriginal);
    verify(projetoRepository).save(projetoOriginal);
    verify(modelMapper).map(projetoOriginal, ProjetoDTO.class);
  }

  // Projeto inexistente deve lançar exceção 404
  @Test
  void deveLancarExcecaoSeProjetoNaoEncontrado() {
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Teste");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.empty());
    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, servidorId));

    assertEquals(404, ex.getStatusCode().value());
  }

  // Projeto já cancelado deve lançar exceção 400
  @Test
  void deveLancarExcecaoSeProjetoJaCancelado() {
    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setStatus(StatusProjeto.CANCELADO);
    projeto.setEquipeExecutora(Collections.singletonList(servidor));

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, servidorId));

    assertEquals(400, ex.getStatusCode().value());
  }

  // Equipe nula deve lançar exceção 400
  @Test
  void deveLancarExcecaoSeEquipeForNula() {
    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
    projeto.setEquipeExecutora(null);

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, servidorId));

    assertEquals(400, ex.getStatusCode().value());
  }

  // Equipe vazia deve lançar exceção 400
  @Test
  void deveLancarExcecaoSeEquipeForVazia() {
    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
    projeto.setEquipeExecutora(Collections.emptyList());

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, servidorId));

    assertEquals(400, ex.getStatusCode().value());
  }

  // Aluno na equipe (sem SIAPE) deve lançar exceção 403
  @Test
  void deveLancarExcecaoSeUsuarioNaoForServidor() {
    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
    projeto.setEquipeExecutora(Collections.singletonList(aluno));

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> projetoService.cancelar(projetoId, dto, alunoId));

    assertEquals(403, ex.getStatusCode().value());
  }

  // Atualização de projeto com sucesso
  @Test
  void deveAtualizarProjetoComSucesso() {
    Projeto projeto = new Projeto();
    projeto.setId(projetoId);

    ProjetoDTO dto = new ProjetoDTO();
    dto.setTitulo("Novo Título");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));
    doAnswer(
            invocation -> {
              ProjetoDTO source = invocation.getArgument(0);
              Projeto destino = invocation.getArgument(1);
              destino.setTitulo(source.getTitulo());
              return null;
            })
        .when(modelMapper)
        .map(any(ProjetoDTO.class), any(Projeto.class));
    when(projetoRepository.save(any(Projeto.class))).thenReturn(projeto);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(dto);

    ProjetoDTO resultado = projetoService.atualizarProjeto(projetoId, dto);

    assertNotNull(resultado);
    assertEquals("Novo Título", resultado.getTitulo());
  }

  // Atualização de projeto inexistente deve lançar exceção
  @Test
  void deveLancarExcecaoSeProjetoInexistenteNaAtualizacao() {
    ProjetoDTO dto = new ProjetoDTO();
    dto.setTitulo("Teste");

    when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

    EntityNotFoundException ex =
        assertThrows(
            EntityNotFoundException.class, () -> projetoService.atualizarProjeto(99L, dto));

    assertEquals("Projeto com ID 99 não encontrado.", ex.getMessage());
  }

  @Test
  void atualizarProjeto_quandoProjetoNaoExiste_deveLancarEntityNotFoundException() {
    Long idInexistente = 99L;
    ProjetoDTO dadosParaAtualizar = new ProjetoDTO();
    String mensagemErro = "Projeto com ID " + idInexistente + " não encontrado.";

    when(projetoRepository.findById(idInexistente)).thenReturn(Optional.empty());

    EntityNotFoundException exception =
        assertThrows(
            EntityNotFoundException.class,
            () -> projetoService.atualizarProjeto(idInexistente, dadosParaAtualizar));

    assertEquals(mensagemErro, exception.getMessage());

    verify(projetoRepository, never()).save(any());
    verify(modelMapper, never()).map(any(), any());
  }

  @Test
  void findAll_quandoExistemProjetos_deveRetornarListaDeProjetos() {
    Projeto projeto1 = new Projeto();
    projeto1.setId(1L);
    Projeto projeto2 = new Projeto();
    projeto2.setId(2L);
    List<Projeto> listaDeProjetos = List.of(projeto1, projeto2);

    when(projetoRepository.findAll()).thenReturn(listaDeProjetos);

    List<Projeto> resultado = projetoService.findAll();

    assertNotNull(resultado);
    assertEquals(2, resultado.size());
    assertEquals(listaDeProjetos, resultado);
    verify(projetoRepository).findAll();
  }

  @Test
  void delete_quandoIdFornecido_deveChamarDeleteByIdDoRepositorio() {
    Long projetoIdParaDeletar = 1L;
    doNothing().when(projetoRepository).deleteById(projetoIdParaDeletar);

    projetoService.delete(projetoIdParaDeletar);

    verify(projetoRepository).deleteById(projetoIdParaDeletar);
    verify(projetoRepository, times(1)).deleteById(projetoIdParaDeletar);
  }

  @Test
  void preSave_quandoResponsavelNaoInformado_deveAtribuirUsuarioAutenticado() {
    Projeto projeto = criaProjetoGenerico();

    String emailAutenticado = "user@utfpr.edu.br";
    Usuario usuarioMock = new Usuario();
    usuarioMock.setId(1L);
    usuarioMock.setEmail(emailAutenticado);
    projeto.getEquipeExecutora().add(usuarioMock);

    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn(emailAutenticado);

    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(usuarioRepository.findByEmail(emailAutenticado)).thenReturn(Optional.of(usuarioMock));

    Projeto resultado = projetoService.preSave(projeto);

    assertNotNull(resultado.getResponsavel());
    assertEquals(usuarioMock, resultado.getResponsavel());
    verify(usuarioRepository, times(2)).findByEmail(emailAutenticado);

    SecurityContextHolder.clearContext();
  }

  @Test
  void presave_QuandoUpdateContendoIdNaoNulo_DeveDevolverProjetoSemAlteracaoDeStatus() {
    String email = "test@utfpr.edu.br";
    Projeto projeto = criaProjetoGenerico();
    projeto.setId(1L);
    projeto.setStatus(StatusProjeto.FINALIZADO);
    Usuario usuario = Usuario.builder().email(email).build();
    projeto.getEquipeExecutora().add(usuario);

    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
    Projeto resultado = projetoService.preSave(projeto);
    assertEquals(StatusProjeto.FINALIZADO, resultado.getStatus());
  }

  @Test
  @DisplayName("preSave não deve processar imagem quando imagemUrl for nulo")
  void preSave_quandoImagemUrlNulo_naoDeveProcessarImagem() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setImagemUrl(null);
    projeto.setEquipeExecutora(new ArrayList<>()); // Para evitar NPE em outros métodos

    // Mock do comportamento de outros métodos chamados no preSave
    Authentication authentication = mock(Authentication.class);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    when(authentication.isAuthenticated())
        .thenReturn(false); // Supõe que não há usuário autenticado para simplicidade

    // Act
    projetoService.preSave(projeto);

    // Assert
    verify(imageUtils, never()).validateAndDecodeBase64Image(any());
    verify(fileService, never()).store(any(), any(), any());
    assertNull(projeto.getImagemUrl());

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("preSave não deve alterar imagemUrl quando for uma URL HTTP")
  void preSave_quandoImagemUrlForHttp_naoDeveAlterarUrl() {
    // Arrange
    String httpUrl = "http://example.com/image.png";
    Projeto projeto = new Projeto();
    projeto.setImagemUrl(httpUrl);
    projeto.setEquipeExecutora(new ArrayList<>());

    when(imageUtils.validateAndDecodeBase64Image(httpUrl)).thenReturn(null);
    // Mock do comportamento de outros métodos chamados no preSave
    Authentication authentication = mock(Authentication.class);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    when(authentication.isAuthenticated()).thenReturn(false);

    // Act
    projetoService.preSave(projeto);

    // Assert
    verify(imageUtils, times(1)).validateAndDecodeBase64Image(httpUrl);
    verify(fileService, never()).store(any(), any(), any());
    assertEquals(httpUrl, projeto.getImagemUrl());

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("preSave deve lançar ResponseStatusException se o armazenamento do arquivo falhar")
  void preSave_quandoFileServiceLancaExcecao_deveLancarResponseStatusException() {
    // Arrange
    String base64Image = "data:image/png;base64,valid-base64-string";
    Projeto projeto = new Projeto();
    projeto.setImagemUrl(base64Image);
    projeto.setEquipeExecutora(new ArrayList<>());

    byte[] imageData = new byte[] {1, 2, 3};
    String contentType = "image/png";
    ImageUtils.DecodedImage decodedImage = new ImageUtils.DecodedImage(imageData, contentType);

    when(imageUtils.validateAndDecodeBase64Image(base64Image)).thenReturn(decodedImage);
    when(fileService.store(any(), any(), any()))
        .thenThrow(new RuntimeException("Erro de armazenamento"));

    try (var mockedStatic = mockStatic(ImageUtils.class)) {
      mockedStatic
          .when(() -> ImageUtils.getFileExtensionFromMimeType(contentType))
          .thenReturn("png");

      Authentication authentication = mock(Authentication.class);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      when(authentication.isAuthenticated()).thenReturn(false);

      // Act & Assert
      ResponseStatusException exception =
          assertThrows(ResponseStatusException.class, () -> projetoService.preSave(projeto));

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
      assertEquals("Falha ao processar a imagem do projeto.", exception.getReason());

      verify(imageUtils).validateAndDecodeBase64Image(base64Image);
      verify(fileService).store(imageData, contentType, "projeto-imagem.png");
      mockedStatic.verify(() -> ImageUtils.getFileExtensionFromMimeType(contentType));
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  @Test
  @DisplayName("preSave não deve processar imagem quando imagemUrl for uma string vazia")
  void preSave_quandoImagemUrlVazia_naoDeveProcessarImagem() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setImagemUrl("");
    projeto.setEquipeExecutora(new ArrayList<>());

    Authentication authentication = mock(Authentication.class);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    when(authentication.isAuthenticated()).thenReturn(false);

    // Act
    projetoService.preSave(projeto);

    // Assert
    verify(imageUtils, never()).validateAndDecodeBase64Image(anyString());
    verify(fileService, never()).store(any(), any(), any());

    assertEquals("", projeto.getImagemUrl());

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("preSave deve processar e atualizar a URL da imagem quando for um Base64 válido")
  void preSave_quandoImagemBase64Valida_deveAtualizarUrl() {
    // Arrange
    String base64Image = "data:image/png;base64,valid-base64-string";
    String finalUrl = "http://storage/projeto-imagem.png";
    Projeto projeto = new Projeto();
    projeto.setImagemUrl(base64Image);
    projeto.setEquipeExecutora(new ArrayList<>());

    byte[] imageData = new byte[] {1, 2, 3};
    String contentType = "image/png";
    ImageUtils.DecodedImage decodedImage = new ImageUtils.DecodedImage(imageData, contentType);
    FileInfoDTO fileInfoDTO =
        new FileInfoDTO(
            "projeto-imagem.png",
            "projeto-imagem.png",
            contentType,
            3L,
            finalUrl,
            LocalDateTime.now());

    when(imageUtils.validateAndDecodeBase64Image(base64Image)).thenReturn(decodedImage);
    when(fileService.store(imageData, contentType, "projeto-imagem.png")).thenReturn(fileInfoDTO);

    try (var mockedStatic = mockStatic(ImageUtils.class)) {
      mockedStatic
          .when(() -> ImageUtils.getFileExtensionFromMimeType(contentType))
          .thenReturn("png");

      // Mock do comportamento de outros métodos chamados no preSave
      Authentication authentication = mock(Authentication.class);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      when(authentication.isAuthenticated()).thenReturn(false);

      // Act
      projetoService.preSave(projeto);

      // Assert
      assertEquals(finalUrl, projeto.getImagemUrl());
      verify(imageUtils).validateAndDecodeBase64Image(base64Image);
      verify(fileService).store(imageData, contentType, "projeto-imagem.png");
      mockedStatic.verify(() -> ImageUtils.getFileExtensionFromMimeType(contentType));
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  @Test
  @DisplayName("Deve lançar ResponseStatusException quando usuário da equipe for nulo")
  void atribuirUsuariosEquipeExecutora_quandoUsuarioNulo_deveLancarExcecao() {
    Projeto projeto = criaProjetoGenerico();
    projeto.getEquipeExecutora().add(null);

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> projetoService.preSave(projeto));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Usuário inválido na requisição", exception.getReason());
  }

  @ParameterizedTest
  @DisplayName("Deve lançar ResponseStatusException quando email do usuário for inválido")
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  void atribuirUsuariosEquipeExecutora_quandoEmailInvalido_deveLancarExcecao(String email) {
    Projeto projeto = criaProjetoGenerico();
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    projeto.getEquipeExecutora().add(usuario);

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> projetoService.preSave(projeto));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Usuário com email não informado", exception.getReason());
  }

  @Test
  @DisplayName("Deve lançar ResponseStatusException quando usuário não for encontrado pelo email")
  void atribuirUsuariosEquipeExecutora_quandoUsuarioNaoEncontrado_deveLancarExcecao() {
    Projeto projeto = criaProjetoGenerico();
    Usuario usuario = new Usuario();
    usuario.setEmail("naoexiste@utfpr.edu.br");
    projeto.getEquipeExecutora().add(usuario);

    when(usuarioRepository.findByEmail("naoexiste@utfpr.edu.br")).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> projetoService.preSave(projeto));

    assertEquals(HttpStatus.NOT_ACCEPTABLE, exception.getStatusCode());
    assertEquals("Usuário com email naoexiste@utfpr.edu.br não encontrado.", exception.getReason());
  }

  @Test
  @DisplayName("Deve carregar usuários da equipe executora com sucesso")
  void atribuirUsuariosEquipeExecutora_quandoEmailsValidos_deveCarregarUsuarios() {
    Projeto projeto = criaProjetoGenerico();
    Usuario usuarioInput = new Usuario();
    usuarioInput.setEmail("user1@utfpr.edu.br");
    projeto.getEquipeExecutora().add(usuarioInput);

    Usuario usuarioCarregado = new Usuario();
    usuarioCarregado.setId(1L);
    usuarioCarregado.setEmail("user1@utfpr.edu.br");
    usuarioCarregado.setNome("Usuario 1");

    when(usuarioRepository.findByEmail("user1@utfpr.edu.br"))
        .thenReturn(Optional.of(usuarioCarregado));

    Authentication authentication = mock(Authentication.class);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    when(authentication.isAuthenticated()).thenReturn(false);

    Projeto resultado = projetoService.preSave(projeto);

    assertEquals(1, resultado.getEquipeExecutora().size());
    assertEquals(usuarioCarregado, resultado.getEquipeExecutora().getFirst());
    verify(usuarioRepository).findByEmail("user1@utfpr.edu.br");

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Deve carregar múltiplos usuários da equipe executora com sucesso")
  void atribuirUsuariosEquipeExecutora_quandoMultiplosEmailsValidos_deveCarregarTodosUsuarios() {
    Projeto projeto = criaProjetoGenerico();

    Usuario usuarioInput1 = new Usuario();
    usuarioInput1.setEmail("user1@utfpr.edu.br");
    Usuario usuarioInput2 = new Usuario();
    usuarioInput2.setEmail("user2@utfpr.edu.br");

    projeto.getEquipeExecutora().add(usuarioInput1);
    projeto.getEquipeExecutora().add(usuarioInput2);

    Usuario usuarioCarregado1 = new Usuario();
    usuarioCarregado1.setId(1L);
    usuarioCarregado1.setEmail("user1@utfpr.edu.br");

    Usuario usuarioCarregado2 = new Usuario();
    usuarioCarregado2.setId(2L);
    usuarioCarregado2.setEmail("user2@utfpr.edu.br");

    when(usuarioRepository.findByEmail("user1@utfpr.edu.br"))
        .thenReturn(Optional.of(usuarioCarregado1));
    when(usuarioRepository.findByEmail("user2@utfpr.edu.br"))
        .thenReturn(Optional.of(usuarioCarregado2));

    Authentication authentication = mock(Authentication.class);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    when(authentication.isAuthenticated()).thenReturn(false);

    Projeto resultado = projetoService.preSave(projeto);

    assertEquals(2, resultado.getEquipeExecutora().size());
    assertTrue(resultado.getEquipeExecutora().contains(usuarioCarregado1));
    assertTrue(resultado.getEquipeExecutora().contains(usuarioCarregado2));
    verify(usuarioRepository).findByEmail("user1@utfpr.edu.br");
    verify(usuarioRepository).findByEmail("user2@utfpr.edu.br");

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Não deve alterar o responsável quando já estiver definido")
  void atribuirResponsavel_quandoResponsavelJaDefinido_naoDeveAlterar() {
    String emailAutenticado = "user@utfpr.edu.br";
    Usuario responsavelExistente = new Usuario();
    responsavelExistente.setId(10L);
    responsavelExistente.setEmail("responsavel@utfpr.edu.br");

    Projeto projeto = criaProjetoGenerico();
    projeto.setResponsavel(responsavelExistente);
    projeto.getEquipeExecutora().add(new Usuario());
    projeto.getEquipeExecutora().getFirst().setEmail(emailAutenticado);

    Authentication authentication = mock(Authentication.class);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn(emailAutenticado);

    Usuario usuarioAutenticado = new Usuario();
    usuarioAutenticado.setId(1L);
    usuarioAutenticado.setEmail(emailAutenticado);
    when(usuarioRepository.findByEmail(emailAutenticado))
        .thenReturn(Optional.of(usuarioAutenticado));

    Projeto resultado = projetoService.preSave(projeto);

    assertEquals(responsavelExistente, resultado.getResponsavel());
    assertNotEquals(usuarioAutenticado, resultado.getResponsavel());

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Deve lançar EntityNotFoundException quando usuário autenticado não for encontrado")
  void atribuirResponsavel_quandoUsuarioAutenticadoNaoEncontrado_deveLancarExcecao() {
    String emailAutenticado = "inexistente@utfpr.edu.br";

    Projeto projeto = criaProjetoGenerico();
    projeto.setResponsavel(null);
    projeto.getEquipeExecutora().add(new Usuario());
    projeto.getEquipeExecutora().getFirst().setEmail(emailAutenticado);

    Authentication authentication = mock(Authentication.class);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn(emailAutenticado);

    when(usuarioRepository.findByEmail(emailAutenticado)).thenReturn(Optional.empty());

    EntityNotFoundException exception =
        assertThrows(EntityNotFoundException.class, () -> projetoService.preSave(projeto));

    assertEquals("Usuário autenticado não encontrado", exception.getMessage());

    SecurityContextHolder.clearContext();
  }

  @Test
  void buscarProjetosPorFiltro_quandoFiltroDeTituloInformado_deveChamarRepositorio() {
    // Arrange
    FiltroProjetoDTO filtro = new FiltroProjetoDTO("Robótica", null, null, null, null, null, null);
    List<Projeto> listaResultado = List.of(projetoMock);

    when(projetoRepository.findAll(any(Specification.class))).thenReturn(listaResultado);
    when(modelMapper.map(projetoMock, ProjetoDTO.class)).thenReturn(projetoDTOMock);

    // Act
    List<ProjetoDTO> resultado = projetoService.buscarProjetosPorFiltro(filtro);

    // Assert
    assertNotNull(resultado);
    assertFalse(resultado.isEmpty());
    assertEquals(1, resultado.size());
    verify(projetoRepository).findAll(any(Specification.class));
  }

  @Test
  void buscarProjetosPorFiltro_quandoFiltroDeStatusInformado_deveChamarRepositorio() {
    // Arrange
    FiltroProjetoDTO filtro =
        new FiltroProjetoDTO(null, StatusProjeto.EM_ANDAMENTO, null, null, null, null, null);
    List<Projeto> listaResultado = List.of(projetoMock);

    when(projetoRepository.findAll(any(Specification.class))).thenReturn(listaResultado);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(projetoDTOMock);

    // Act
    List<ProjetoDTO> resultado = projetoService.buscarProjetosPorFiltro(filtro);

    // Assert
    assertNotNull(resultado);
    assertFalse(resultado.isEmpty());
    verify(projetoRepository).findAll(any(Specification.class));
  }

  @Test
  void buscarProjetosPorFiltro_quandoFiltroDeDataInicioDeInformado_deveChamarRepositorio() {
    // Arrange
    FiltroProjetoDTO filtro =
        new FiltroProjetoDTO(null, null, LocalDate.now(), null, null, null, null);
    List<Projeto> listaResultado = List.of(projetoMock);

    when(projetoRepository.findAll(any(Specification.class))).thenReturn(listaResultado);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(projetoDTOMock);

    // Act
    List<ProjetoDTO> resultado = projetoService.buscarProjetosPorFiltro(filtro);

    // Assert
    assertNotNull(resultado);
    assertFalse(resultado.isEmpty());
    verify(projetoRepository).findAll(any(Specification.class));
  }

  @Test
  void buscarProjetosPorFiltro_quandoFiltroDeDataInicioAteInformado_deveChamarRepositorio() {
    // Arrange
    FiltroProjetoDTO filtro =
        new FiltroProjetoDTO(null, null, null, LocalDate.now(), null, null, null);
    List<Projeto> listaResultado = List.of(projetoMock);

    when(projetoRepository.findAll(any(Specification.class))).thenReturn(listaResultado);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(projetoDTOMock);

    // Act
    List<ProjetoDTO> resultado = projetoService.buscarProjetosPorFiltro(filtro);

    // Assert
    assertNotNull(resultado);
    assertFalse(resultado.isEmpty());
    verify(projetoRepository).findAll(any(Specification.class));
  }

  @Test
  void buscarProjetosPorFiltro_quandoFiltroDeIdResponsavelInformado_deveChamarRepositorio() {
    // Arrange
    FiltroProjetoDTO filtro = new FiltroProjetoDTO(null, null, null, null, 1L, null, null);
    List<Projeto> listaResultado = List.of(projetoMock);

    when(projetoRepository.findAll(any(Specification.class))).thenReturn(listaResultado);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(projetoDTOMock);

    // Act
    List<ProjetoDTO> resultado = projetoService.buscarProjetosPorFiltro(filtro);

    // Assert
    assertNotNull(resultado);
    assertFalse(resultado.isEmpty());
    verify(projetoRepository).findAll(any(Specification.class));
  }

  @Test
  void buscarProjetosPorFiltro_quandoFiltroDeIdMembroEquipeInformado_deveChamarRepositorio() {
    // Arrange
    FiltroProjetoDTO filtro = new FiltroProjetoDTO(null, null, null, null, null, 2L, null);
    List<Projeto> listaResultado = List.of(projetoMock);

    when(projetoRepository.findAll(any(Specification.class))).thenReturn(listaResultado);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(projetoDTOMock);

    // Act
    List<ProjetoDTO> resultado = projetoService.buscarProjetosPorFiltro(filtro);

    // Assert
    assertNotNull(resultado);
    assertFalse(resultado.isEmpty());
    verify(projetoRepository).findAll(any(Specification.class));
  }

  @Test
  void buscarProjetosPorFiltro_quandoFiltroDeIdCursoInformado_deveChamarRepositorio() {
    // Arrange
    FiltroProjetoDTO filtro = new FiltroProjetoDTO(null, null, null, null, null, null, 3L);
    List<Projeto> listaResultado = List.of(projetoMock);

    when(projetoRepository.findAll(any(Specification.class))).thenReturn(listaResultado);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(projetoDTOMock);

    // Act
    List<ProjetoDTO> resultado = projetoService.buscarProjetosPorFiltro(filtro);

    // Assert
    assertNotNull(resultado);
    assertFalse(resultado.isEmpty());
    verify(projetoRepository).findAll(any(Specification.class));
  }

  @Test
  @DisplayName("postsave deve publicar evento ProjetoCriado para projetos novos (ID nulo)")
  void postsave_quandoProjetoIdNulo_devePublicarEventoProjetoCriado() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(null);

    // Act
    projetoService.postsave(projeto);

    // Assert
    verify(eventPublisher).publishProjetoCriado(projeto);
    verify(eventPublisher, never()).publishProjetoAtualizado(any());
  }

  @Test
  @DisplayName("postsave deve publicar evento ProjetoCriado para projetos com ID zero")
  void postsave_quandoProjetoIdZero_devePublicarEventoProjetoCriado() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(0L);

    // Act
    projetoService.postsave(projeto);

    // Assert
    verify(eventPublisher).publishProjetoCriado(projeto);
    verify(eventPublisher, never()).publishProjetoAtualizado(any());
  }

  @Test
  @DisplayName("postsave deve publicar evento ProjetoCriado para projetos com ID negativo")
  void postsave_quandoProjetoIdNegativo_devePublicarEventoProjetoCriado() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(-1L);

    // Act
    projetoService.postsave(projeto);

    // Assert
    verify(eventPublisher).publishProjetoCriado(projeto);
    verify(eventPublisher, never()).publishProjetoAtualizado(any());
  }

  @Test
  @DisplayName("postsave deve publicar evento ProjetoAtualizado para projetos existentes")
  void postsave_quandoProjetoIdPositivo_devePublicarEventoProjetoAtualizado() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(1L);

    // Act
    projetoService.postsave(projeto);

    // Assert
    verify(eventPublisher).publishProjetoAtualizado(projeto);
    verify(eventPublisher, never()).publishProjetoCriado(any());
  }

  @NotNull private static Projeto criaProjetoGenerico() {
    Projeto projeto = new Projeto();
    projeto.setTitulo("Projeto Teste");
    projeto.setDescricao("Descrição");
    projeto.setJustificativa("Justificativa");
    projeto.setDataInicio(new Date());
    projeto.setPublicoAlvo("Alunos");
    projeto.setVinculadoDisciplina(false);
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
    projeto.setEquipeExecutora(new ArrayList<>());
    return projeto;
  }
}
