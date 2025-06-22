package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceImplTest {

  @InjectMocks private ProjetoServiceImpl projetoService;

  @Mock private ProjetoRepository projetoRepository;
  @Mock private ModelMapper modelMapper;
  @Mock private UsuarioRepository usuarioRepository;

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
    Long projetoId = 1L;

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
    Projeto projeto = new Projeto();
    projeto.setTitulo("Projeto Teste");
    projeto.setDescricao("Descrição");
    projeto.setJustificativa("Justificativa");
    projeto.setDataInicio(new Date());
    projeto.setPublicoAlvo("Alunos");
    projeto.setVinculadoDisciplina(false);
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);

    String emailAutenticado = "user@utfpr.edu.br";
    Usuario usuarioMock = new Usuario();
    usuarioMock.setId(1L);
    usuarioMock.setEmail(emailAutenticado);

    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn(emailAutenticado);

    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(usuarioRepository.findByEmail(emailAutenticado)).thenReturn(Optional.of(usuarioMock));

    Projeto resultado = projetoService.preSave(projeto);

    assertNotNull(resultado.getResponsavel());
    assertEquals(usuarioMock, resultado.getResponsavel());
    verify(usuarioRepository).findByEmail(emailAutenticado);

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
}
