package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceImplTest {

  @InjectMocks private ProjetoServiceImpl projetoService;

  @Mock private ProjetoRepository projetoRepository;

  @Mock private ModelMapper modelMapper;

  private final Long projetoId = 1L;
  private final Long servidorId = 100L;
  private final Long alunoId = 200L;

  private Usuario servidor;
  private Usuario aluno;

  @BeforeEach
  void setUp() {
    servidor = new Usuario();
    servidor.setId(servidorId);
    servidor.setSiape("1234567");

    aluno = new Usuario();
    aluno.setId(alunoId);
    aluno.setRegistroAcademico("20230123");
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
}
