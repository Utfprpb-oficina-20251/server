package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioProjetoDTO;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProjetoControllerTest {

  @InjectMocks private ProjetoController projetoController;

  @Mock private IProjetoService projetoService;

  @Mock private UsuarioRepository usuarioRepository;

  @Mock private ModelMapper modelMapper;

  private ProjetoDTO projetoDTOEntrada;
  private Usuario usuario;
  private Projeto projeto;
  private Projeto projetoSalvo;

  @BeforeEach
  void setUp() {
    // Dados comuns para os testes
    UsuarioProjetoDTO membroEquipe = new UsuarioProjetoDTO();
    membroEquipe.setEmail("membro@utfpr.edu.br");

    projetoDTOEntrada = new ProjetoDTO();
    projetoDTOEntrada.setTitulo("Projeto Teste");
    projetoDTOEntrada.setDescricao("Descrição do projeto");
    projetoDTOEntrada.setEquipeExecutora(List.of(membroEquipe));

    usuario = new Usuario();
    usuario.setId(1L);
    usuario.setEmail("membro@utfpr.edu.br");
    usuario.setNome("Membro Teste");

    projeto = new Projeto();
    projeto.setTitulo("Projeto Teste");
    projeto.setEquipeExecutora(List.of(usuario));
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);

    projetoSalvo = new Projeto();
    projetoSalvo.setId(1L);
    projetoSalvo.setTitulo("Projeto Teste");
    projetoSalvo.setEquipeExecutora(List.of(usuario));
    projetoSalvo.setStatus(StatusProjeto.EM_ANDAMENTO);
  }

  @Test
  void shouldCreateProjetoSuccessfully() {
    // Arrange
    ProjetoDTO projetoDTO = new ProjetoDTO();
    projetoDTO.setTitulo("Projeto Teste");
    projetoDTO.setEquipeExecutora(
        List.of(UsuarioProjetoDTO.builder().nome("batata").email("batata@utfpr.edu.br").build()));

    usuario = new Usuario();
    usuario.setEmail("batata@utfpr.edu.br");

    when(modelMapper.map(any(ProjetoDTO.class), eq(Projeto.class))).thenReturn(projeto);
    when(projetoService.save(any(Projeto.class))).thenReturn(projeto);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(projetoDTO);

    // Act
    ResponseEntity<ProjetoDTO> response = projetoController.create(projetoDTO);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Projeto Teste", response.getBody().getTitulo());
    verify(projetoService).save(any(Projeto.class));
    verify(modelMapper).map(any(Projeto.class), eq(ProjetoDTO.class));
  }

  @Test
  void update_quandoProjetoExiste_retornaOkEProjetoDTO() {
    // Arrange (Organização)
    Long projetoId = 1L;

    ProjetoDTO dtoRequest = new ProjetoDTO();
    dtoRequest.setTitulo("Título Atualizado");

    ProjetoDTO dtoResponse = new ProjetoDTO();
    dtoResponse.setId(projetoId);
    dtoResponse.setTitulo("Título Atualizado");

    when(projetoService.atualizarProjeto(eq(projetoId), any(ProjetoDTO.class)))
        .thenReturn(dtoResponse);

    // Act (Ação)
    ResponseEntity<ProjetoDTO> response = projetoController.update(projetoId, dtoRequest);

    // Assert (Verificação)
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(dtoResponse, response.getBody());
    verify(projetoService).atualizarProjeto(eq(projetoId), any(ProjetoDTO.class));
  }

  @Test
  void update_quandoProjetoNaoExiste_deveLancarEntityNotFoundException() {
    // Arrange (Organização)
    Long projetoIdInexistente = 99L;
    String mensagemErro = "Projeto com ID " + projetoIdInexistente + " não encontrado.";

    ProjetoDTO dtoRequest = new ProjetoDTO();
    dtoRequest.setTitulo("Qualquer Título");

    when(projetoService.atualizarProjeto(eq(projetoIdInexistente), any(ProjetoDTO.class)))
        .thenThrow(new EntityNotFoundException(mensagemErro));

    // Act & Assert (Ação e Verificação)
    EntityNotFoundException exception =
        assertThrows(
            EntityNotFoundException.class,
            () -> projetoController.update(projetoIdInexistente, dtoRequest));

    assertEquals(mensagemErro, exception.getMessage());
    verify(projetoService).atualizarProjeto(eq(projetoIdInexistente), any(ProjetoDTO.class));
  }

  @Test
  void create_deveCriarProjeto_quandoDadosSaoValidos() {
    // Arrange (Organizar)
    when(projetoService.save(any(Projeto.class))).thenReturn(projetoSalvo);
    when(modelMapper.map(any(ProjetoDTO.class), eq(Projeto.class))).thenReturn(projetoSalvo);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class)))
        .thenReturn(new ProjetoDTO()); // Retorna um DTO mockado/novo

    // Act (Agir)
    ResponseEntity<ProjetoDTO> response = projetoController.create(projetoDTOEntrada);

    // Assert (Verificar)
    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    verify(projetoService, times(1)).save(any(Projeto.class));
    verify(modelMapper, times(1)).map(any(Projeto.class), eq(ProjetoDTO.class));
  }

  @Test
  void create_deveLancarResponseStatusException_quandoEquipeExecutoraEVazia() {
    // Arrange
    projetoDTOEntrada.setEquipeExecutora(new ArrayList<>()); // Lista de e-mails vazia

    // Act + Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> projetoController.create(projetoDTOEntrada));

    assertEquals(HttpStatus.NOT_ACCEPTABLE, exception.getStatusCode());
    assertEquals("A equipe executora não pode estar vazia.", exception.getReason());

    // Verifica que os serviços principais nunca foram chamados
    verify(usuarioRepository, never()).findByEmail(anyString());
    verify(projetoService, never()).save(any(Projeto.class));
  }

  @Test
  void create_deveLancarResponseStatusException_quandoEquipeExecutoraENula() {
    projetoDTOEntrada.setEquipeExecutora(null);

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> projetoController.create(projetoDTOEntrada));

    assertEquals(HttpStatus.NOT_ACCEPTABLE, exception.getStatusCode());
    assertEquals("A equipe executora não pode estar vazia.", exception.getReason());
  }

  @Test
  void create_comImagemUrlBase64Valida_deveCriarProjetoComImagemProcessada() {
    // Arrange
    String base64Image =
        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/wcAAwAB/epv2AAAAABJRU5ErkJggg==";
    String finalUrl = "http://storage/projeto-imagem.png";

    projetoDTOEntrada.setImagemUrl(base64Image);

    Projeto projetoComImagemProcessada = new Projeto();
    projetoComImagemProcessada.setId(1L);
    projetoComImagemProcessada.setImagemUrl(finalUrl);

    ProjetoDTO projetoDTOResponse = new ProjetoDTO();
    projetoDTOResponse.setId(1L);
    projetoDTOResponse.setImagemUrl(finalUrl);

    when(modelMapper.map(any(ProjetoDTO.class), eq(Projeto.class))).thenReturn(projeto);
    when(projetoService.save(any(Projeto.class))).thenReturn(projetoComImagemProcessada);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(projetoDTOResponse);

    // Act
    ResponseEntity<ProjetoDTO> response = projetoController.create(projetoDTOEntrada);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(finalUrl, response.getBody().getImagemUrl());
    verify(projetoService).save(any(Projeto.class));
  }

  @Test
  void create_comImagemUrlHttpValida_deveCriarProjetoSemProcessamento() {
    // Arrange
    String httpImageUrl = "https://example.com/image.jpg";

    projetoDTOEntrada.setImagemUrl(httpImageUrl);

    Projeto projetoComImagemHttp = new Projeto();
    projetoComImagemHttp.setId(1L);
    projetoComImagemHttp.setImagemUrl(httpImageUrl);

    ProjetoDTO projetoDTOResponse = new ProjetoDTO();
    projetoDTOResponse.setId(1L);
    projetoDTOResponse.setImagemUrl(httpImageUrl);

    when(modelMapper.map(any(ProjetoDTO.class), eq(Projeto.class))).thenReturn(projeto);
    when(projetoService.save(any(Projeto.class))).thenReturn(projetoComImagemHttp);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(projetoDTOResponse);

    // Act
    ResponseEntity<ProjetoDTO> response = projetoController.create(projetoDTOEntrada);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(httpImageUrl, response.getBody().getImagemUrl());
    verify(projetoService).save(any(Projeto.class));
  }

  @Test
  void create_comImagemUrlNula_deveCriarProjetoSemImagem() {
    // Arrange
    projetoDTOEntrada.setImagemUrl(null);

    when(modelMapper.map(any(ProjetoDTO.class), eq(Projeto.class))).thenReturn(projeto);
    when(projetoService.save(any(Projeto.class))).thenReturn(projetoSalvo);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(new ProjetoDTO());

    // Act
    ResponseEntity<ProjetoDTO> response = projetoController.create(projetoDTOEntrada);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(projetoService).save(any(Projeto.class));
  }

  @Test
  void create_comImagemUrlVazia_deveCriarProjetoSemImagem() {
    // Arrange
    projetoDTOEntrada.setImagemUrl("");

    when(modelMapper.map(any(ProjetoDTO.class), eq(Projeto.class))).thenReturn(projeto);
    when(projetoService.save(any(Projeto.class))).thenReturn(projetoSalvo);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(new ProjetoDTO());

    // Act
    ResponseEntity<ProjetoDTO> response = projetoController.create(projetoDTOEntrada);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(projetoService).save(any(Projeto.class));
  }

  @Test
  void create_comImagemBase64Invalida_deveLancarResponseStatusException() {
    // Arrange
    String imagemBase64Invalida = "data:image/png;base64,invalid-base64";
    projetoDTOEntrada.setImagemUrl(imagemBase64Invalida);

    when(modelMapper.map(any(ProjetoDTO.class), eq(Projeto.class))).thenReturn(projeto);
    when(projetoService.save(any(Projeto.class)))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao processar a imagem do projeto."));

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> projetoController.create(projetoDTOEntrada));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals("Falha ao processar a imagem do projeto.", exception.getReason());
    verify(projetoService).save(any(Projeto.class));
  }

  @Test
  void update_comNovaImagemUrl_deveAtualizarProjetoComImagemProcessada() {
    // Arrange
    Long projetoId = 1L;
    String novaImagemBase64 = "data:image/jpeg;base64,validBase64String";
    String finalUrl = "http://storage/projeto-imagem.jpg";

    ProjetoDTO dtoRequest = new ProjetoDTO();
    dtoRequest.setId(projetoId);
    dtoRequest.setTitulo("Título Atualizado");
    dtoRequest.setImagemUrl(novaImagemBase64);

    ProjetoDTO dtoResponse = new ProjetoDTO();
    dtoResponse.setId(projetoId);
    dtoResponse.setTitulo("Título Atualizado");
    dtoResponse.setImagemUrl(finalUrl);

    when(projetoService.atualizarProjeto(eq(projetoId), any(ProjetoDTO.class)))
        .thenReturn(dtoResponse);

    // Act
    ResponseEntity<ProjetoDTO> response = projetoController.update(projetoId, dtoRequest);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(finalUrl, response.getBody().getImagemUrl());
    verify(projetoService).atualizarProjeto(eq(projetoId), any(ProjetoDTO.class));
  }

  @Test
  void update_removendoImagemUrl_deveAtualizarProjetoSemImagem() {
    // Arrange
    Long projetoId = 1L;

    ProjetoDTO dtoRequest = new ProjetoDTO();
    dtoRequest.setId(projetoId);
    dtoRequest.setTitulo("Título Atualizado");
    dtoRequest.setImagemUrl(null);

    ProjetoDTO dtoResponse = new ProjetoDTO();
    dtoResponse.setId(projetoId);
    dtoResponse.setTitulo("Título Atualizado");
    dtoResponse.setImagemUrl(null);

    when(projetoService.atualizarProjeto(eq(projetoId), any(ProjetoDTO.class)))
        .thenReturn(dtoResponse);

    // Act
    ResponseEntity<ProjetoDTO> response = projetoController.update(projetoId, dtoRequest);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNull(response.getBody().getImagemUrl());
    verify(projetoService).atualizarProjeto(eq(projetoId), any(ProjetoDTO.class));
  }

  @Test
  void cancelarProjeto_deveChamarServicoEretornarNoContent() {
    // Arrange
    Long projetoId = 1L;
    Long usuarioId = 42L;

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa válida");

    Usuario usuarioMock = new Usuario();
    usuarioMock.setId(usuarioId);

    // Simula o service, que não retorna nada
    doNothing().when(projetoService).cancelar(projetoId, dto, usuarioId);

    // Act
    ResponseEntity<Void> response = projetoController.cancelar(projetoId, dto, usuarioMock);

    // Assert
    assertNotNull(response);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(projetoService, times(1)).cancelar(projetoId, dto, usuarioId);
  }

  @Test
  void cancelarProjeto_deveLancarExcecao_quandoJustificativaForVazia() {
    // Arrange
    Long projetoId = 1L;
    Long usuarioId = 42L;

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("   "); // Justificativa inválida

    Usuario usuarioMock = new Usuario();
    usuarioMock.setId(usuarioId);

    doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "A justificativa é obrigatória."))
        .when(projetoService)
        .cancelar(projetoId, dto, usuarioId);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoController.cancelar(projetoId, dto, usuarioMock));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("A justificativa é obrigatória.", exception.getReason());
    verify(projetoService).cancelar(projetoId, dto, usuarioId);
  }

  @Test
  void cancelarProjeto_deveLancarExcecao_quandoProjetoJaCancelado() {
    // Arrange
    Long projetoId = 1L;
    Long usuarioId = 42L;

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Cancelamento repetido");

    Usuario usuarioMock = new Usuario();
    usuarioMock.setId(usuarioId);

    doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Projeto já está cancelado"))
        .when(projetoService)
        .cancelar(projetoId, dto, usuarioId);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoController.cancelar(projetoId, dto, usuarioMock));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Projeto já está cancelado", exception.getReason());
    verify(projetoService).cancelar(projetoId, dto, usuarioId);
  }

  @Test
  void cancelarProjeto_deveLancarExcecao_quandoUsuarioNaoForResponsavel() {
    // Arrange
    Long projetoId = 1L;
    Long usuarioId = 999L;

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Tentativa de usuário não responsável");

    Usuario usuarioMock = new Usuario();
    usuarioMock.setId(usuarioId);

    doThrow(
            new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Usuário não pertence à equipe executora ou não possui SIAPE."))
        .when(projetoService)
        .cancelar(projetoId, dto, usuarioId);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoController.cancelar(projetoId, dto, usuarioMock));

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals(
        "Usuário não pertence à equipe executora ou não possui SIAPE.", exception.getReason());
    verify(projetoService).cancelar(projetoId, dto, usuarioId);
  }

  @Test
  void cancelarProjeto_deveLancarExcecao_quandoProjetoNaoEncontrado() {
    // Arrange
    Long projetoId = 999L;
    Long usuarioId = 42L;

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Cancelamento de projeto inexistente");

    Usuario usuarioMock = new Usuario();
    usuarioMock.setId(usuarioId);

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado"))
        .when(projetoService)
        .cancelar(projetoId, dto, usuarioId);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoController.cancelar(projetoId, dto, usuarioMock));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Projeto não encontrado", exception.getReason());
    verify(projetoService).cancelar(projetoId, dto, usuarioId);
  }

  @Test
  void buscarMeusProjetos_quandoUsuarioNaoTemProjetos_retornaOkComListaVazia() {
    // Arrange (Organização) 🕵️
    Usuario usuarioLogado = new Usuario();
    usuarioLogado.setId(2L);

    when(projetoService.buscarProjetosPorFiltro(any(FiltroProjetoDTO.class)))
        .thenReturn(Collections.emptyList());

    // Act (Ação) 🚀
    ResponseEntity<List<ProjetoDTO>> response = projetoController.buscarMeusProjetos(usuarioLogado);

    // Assert (Verificação) ✅
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());
    verify(projetoService, times(1)).buscarProjetosPorFiltro(any(FiltroProjetoDTO.class));
  }

  @Test
  void buscarProjetos_semFiltros_retornaOkComListaDeProjetos() {
    // Arrange
    FiltroProjetoDTO filtroVazio =
        new FiltroProjetoDTO(null, null, null, null, null, null, null, null, null, null, null);
    List<ProjetoDTO> listaEsperada = List.of(new ProjetoDTO(), new ProjetoDTO());
    when(projetoService.buscarProjetosPorFiltro(filtroVazio)).thenReturn(listaEsperada);

    // Act
    ResponseEntity<List<ProjetoDTO>> response = projetoController.buscarProjetos(filtroVazio);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());

    verify(projetoService).buscarProjetosPorFiltro(filtroVazio);
  }

  @Test
  void buscarProjetos_comFiltroUnicoDeTitulo_retornaOkComListaFiltrada() {
    // Arrange 🕵️‍♂️
    // 1. Cria um filtro específico, apenas com o título.
    FiltroProjetoDTO filtros =
        new FiltroProjetoDTO(
            "Robótica", null, null, null, null, null, null, null, null, null, null);

    // 2. A lista esperada para este filtro específico.
    List<ProjetoDTO> listaFiltrada = List.of(new ProjetoDTO());

    // 3. Configura o mock para retornar a lista filtrada quando chamado com o filtro exato.
    when(projetoService.buscarProjetosPorFiltro(filtros)).thenReturn(listaFiltrada);

    // Act 🚀
    ResponseEntity<List<ProjetoDTO>> response = projetoController.buscarProjetos(filtros);

    // Assert ✅
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());

    // 7. Garante que o serviço foi chamado com o objeto de filtro correto.
    verify(projetoService).buscarProjetosPorFiltro(filtros);
  }

  @Test
  void buscarProjetos_comFiltrosCombinados_retornaOkComListaFiltrada() {
    // Arrange 🕵️‍♂️
    // 1. Cria um filtro combinando status e ID de um membro da equipe.
    FiltroProjetoDTO filtros =
        new FiltroProjetoDTO(
            null, StatusProjeto.EM_ANDAMENTO, null, null, 10L, null, null, null, null, null, null);
    List<ProjetoDTO> listaFiltrada = List.of(new ProjetoDTO());
    when(projetoService.buscarProjetosPorFiltro(filtros)).thenReturn(listaFiltrada);

    // Act 🚀
    ResponseEntity<List<ProjetoDTO>> response = projetoController.buscarProjetos(filtros);

    // Assert ✅
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(projetoService).buscarProjetosPorFiltro(filtros);
  }

  @Test
  void buscarProjetos_quandoNenhumProjetoEncontrado_retornaOkComListaVazia() {
    // Arrange
    FiltroProjetoDTO filtroInexistente =
        new FiltroProjetoDTO(
            "Projeto Inexistente", null, null, null, null, null, null, null, null, null, null);

    when(projetoService.buscarProjetosPorFiltro(filtroInexistente))
        .thenReturn(Collections.emptyList());

    // Act
    ResponseEntity<List<ProjetoDTO>> response = projetoController.buscarProjetos(filtroInexistente);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());

    verify(projetoService).buscarProjetosPorFiltro(filtroInexistente);
  }
}
