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
  void create_deveRetornarNotAcceptable_quandoEquipeExecutoraEVazia() {
    // Arrange
    projetoDTOEntrada.setEquipeExecutora(new ArrayList<>()); // Lista de e-mails vazia

    // Act
    ResponseEntity<ProjetoDTO> response = projetoController.create(projetoDTOEntrada);

    // Assert
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    assertNull(response.getBody());

    // Verifica que os serviços principais nunca foram chamados
    verify(usuarioRepository, never()).findByEmail(anyString());
    verify(projetoService, never()).save(any(Projeto.class));
  }

  @Test
  void create_deveRetornarNotAcceptable_quandoEmailNaoExiste() {
    // Arrange
    // Configura o mock para retornar um Optional vazio, simulando um usuário não encontrado
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    // Act
    ResponseEntity<ProjetoDTO> response = projetoController.create(projetoDTOEntrada);

    // Assert
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    assertNull(response.getBody());

    // Verifica que o repositório foi consultado, mas o serviço de save nunca foi chamado
    verify(usuarioRepository, times(1)).findByEmail("membro@utfpr.edu.br");
    verify(projetoService, never()).save(any(Projeto.class));
  }

  @Test
  void buscarMeusProjetos_quandoUsuarioTemProjetos_retornaOkComListaDeProjetos() {
    // Arrange (Organização) 🕵️‍♂️
    // 1. Cria um usuário simulando o usuário logado
    Usuario usuarioLogado = new Usuario();
    usuarioLogado.setId(1L);

    // 2. Cria a lista de projetos DTO que o serviço deve retornar
    List<ProjetoDTO> listaProjetosDTO = List.of(new ProjetoDTO(), new ProjetoDTO());

    // 3. Simula o comportamento do serviço: quando 'buscarProjetosPorFiltro' for chamado,
    //    retorna a lista criada acima. Usamos 'any' para o filtro, pois a lógica de criação
    //    do filtro está dentro do controller, e queremos apenas garantir que o serviço seja chamado.
    when(projetoService.buscarProjetosPorFiltro(any(FiltroProjetoDTO.class)))
            .thenReturn(listaProjetosDTO);

    // Act (Ação) 🚀
    // 4. Executa o método do controller que estamos testando
    ResponseEntity<List<ProjetoDTO>> response = projetoController.buscarMeusProjetos(usuarioLogado);

    // Assert (Verificação) ✅
    // 5. Verifica se a resposta não é nula e o status HTTP é 200 (OK)
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    // 6. Verifica se o corpo da resposta não é nulo e contém o número esperado de projetos
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());

    // 7. Garante que o método do serviço foi chamado exatamente uma vez
    verify(projetoService, times(1)).buscarProjetosPorFiltro(any(FiltroProjetoDTO.class));
  }

  @Test
  void buscarMeusProjetos_quandoUsuarioNaoTemProjetos_retornaOkComListaVazia() {
    // Arrange (Organização) 🕵️‍♂️
    // 1. Cria um usuário simulando o usuário logado
    Usuario usuarioLogado = new Usuario();
    usuarioLogado.setId(2L);

    // 2. Simula o serviço retornando uma lista vazia
    when(projetoService.buscarProjetosPorFiltro(any(FiltroProjetoDTO.class)))
            .thenReturn(Collections.emptyList());

    // Act (Ação) 🚀
    // 3. Executa o método do controller
    ResponseEntity<List<ProjetoDTO>> response = projetoController.buscarMeusProjetos(usuarioLogado);

    // Assert (Verificação) ✅
    // 4. Verifica se a resposta e o status HTTP estão corretos
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    // 5. Verifica se o corpo da resposta não é nulo e se a lista está vazia
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());

    // 6. Garante que o serviço foi chamado
    verify(projetoService, times(1)).buscarProjetosPorFiltro(any(FiltroProjetoDTO.class));
  }

  @Test
  void buscarProjetos_semFiltros_retornaOkComListaDeProjetos() {
    // Arrange (Organização) 🕵️‍♂️
    // 1. Cria um objeto de filtro vazio, simulando uma busca geral.
    FiltroProjetoDTO filtros = new FiltroProjetoDTO(null, null, null, null, null, null,null);

    // 2. Cria a lista de projetos que esperamos que o serviço retorne.
    List<ProjetoDTO> listaEsperada = List.of(new ProjetoDTO(), new ProjetoDTO());

    // 3. Configura o mock do serviço: quando 'buscarProjetosPorFiltro' for chamado com qualquer
    //    filtro, deve retornar a lista que preparamos.
    when(projetoService.buscarProjetosPorFiltro(any(FiltroProjetoDTO.class))).thenReturn(listaEsperada);

    // Act (Ação) 🚀
    ResponseEntity<List<ProjetoDTO>> response = projetoController.buscarProjetos(filtros);

    // Assert (Verificação) ✅
    assertEquals(HttpStatus.OK, response.getStatusCode());

    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());

    verify(projetoService).buscarProjetosPorFiltro(any(FiltroProjetoDTO.class));
  }

  @Test
  void buscarProjetos_comFiltroUnicoDeTitulo_retornaOkComListaFiltrada() {
    // Arrange 🕵️‍♂️
    // 1. Cria um filtro específico, apenas com o título.
    FiltroProjetoDTO filtros = new FiltroProjetoDTO("Robótica", null, null, null, null, null,null);

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
    FiltroProjetoDTO filtros = new FiltroProjetoDTO(null, StatusProjeto.EM_ANDAMENTO, null, null, 10L, null,null);
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
    // Arrange (Organização) 🕵️‍♂️
    // 1. Cria um filtro que, neste cenário, não encontrará nenhum resultado.
    FiltroProjetoDTO filtros = new FiltroProjetoDTO("Projeto Inexistente", null, null, null, null, null,null);

    // 2. Configura o mock do serviço para retornar uma lista vazia.
    //    Isso simula o "caso de falha" de uma busca: nenhum resultado encontrado.
    when(projetoService.buscarProjetosPorFiltro(any(FiltroProjetoDTO.class)))
            .thenReturn(Collections.emptyList());

    // Act (Ação) 🚀
    ResponseEntity<List<ProjetoDTO>> response = projetoController.buscarProjetos(filtros);

    // Assert (Verificação) ✅
    // 5. O status ainda deve ser OK, pois a busca foi executada com sucesso.
    assertEquals(HttpStatus.OK, response.getStatusCode());

    // 6. O corpo da resposta não deve ser nulo, mas a lista dentro dele deve estar vazia.
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());

    // 7. Garante que o serviço foi chamado.
    verify(projetoService).buscarProjetosPorFiltro(any(FiltroProjetoDTO.class));
  }
}
