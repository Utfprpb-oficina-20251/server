package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioProjetoDTO;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    Projeto projeto = new Projeto();
    projeto.setTitulo("Projeto Teste");

    when(usuarioRepository.findByEmail("batata@utfpr.edu.br")).thenReturn(Optional.of(usuario));
    when(projetoService.save(any(Projeto.class))).thenReturn(projeto);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(projetoDTO);

    // Act
    ResponseEntity<ProjetoDTO> response = projetoController.create(projetoDTO);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Projeto Teste", response.getBody().getTitulo());
    verify(usuarioRepository).findByEmail("batata@utfpr.edu.br");
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
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
    when(projetoService.save(any(Projeto.class))).thenReturn(projetoSalvo);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class)))
        .thenReturn(new ProjetoDTO()); // Retorna um DTO mockado/novo

    // Act (Agir)
    ResponseEntity<ProjetoDTO> response = projetoController.create(projetoDTOEntrada);

    // Assert (Verificar)
    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    verify(usuarioRepository, times(1)).findByEmail("membro@utfpr.edu.br");
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
  void create_deveLancarResponseStatusException_quandoEmailNaoExiste() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    // Act + Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> projetoController.create(projetoDTOEntrada));

    assertEquals(HttpStatus.NOT_ACCEPTABLE, exception.getStatusCode());
    assertNotNull(exception.getReason());
    assertTrue(
        exception
            .getReason()
            .contains("Usuário com e-mail")); // Pode verificar o início da mensagem

    // Verifica que o repositório foi consultado, mas o serviço de save nunca foi chamado
    verify(usuarioRepository, times(1)).findByEmail("membro@utfpr.edu.br");
    verify(projetoService, never()).save(any(Projeto.class));
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
                HttpStatus.FORBIDDEN, "Apenas o responsável principal pode cancelar o projeto."))
        .when(projetoService)
        .cancelar(projetoId, dto, usuarioId);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoController.cancelar(projetoId, dto, usuarioMock));

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("Apenas o responsável principal pode cancelar o projeto.", exception.getReason());
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
}
