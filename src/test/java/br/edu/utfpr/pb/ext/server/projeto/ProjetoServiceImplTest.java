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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceImplTest {

  @InjectMocks private ProjetoServiceImpl projetoService;

  @Mock private ProjetoRepository projetoRepository;

  @Mock private ModelMapper modelMapper;

  private Projeto projeto;
  private final Long projetoId = 1L;
  private final Long usuarioId = 100L;

  @BeforeEach
  void setUp() {
    Usuario usuario = new Usuario();
    usuario.setId(usuarioId);

    projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
    projeto.setEquipeExecutora(Collections.singletonList(usuario));
  }

  // ✅ Cenário feliz
  @Test
  void deveCancelarProjetoComJustificativaValida() {
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa válida");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    projetoService.cancelar(projetoId, dto, usuarioId);

    assertEquals(StatusProjeto.CANCELADO, projeto.getStatus());
    assertEquals("Justificativa válida", projeto.getJustificativaCancelamento());
    verify(projetoRepository).save(projeto);
  }

  // ✅ Validação de justificativa nula
  @Test
  void deveLancarExcecaoSeJustificativaNula() {
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa(null);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, usuarioId));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertTrue(ex.getReason().toLowerCase().contains("justificativa"));
  }

  // ✅ Validação de justificativa vazia
  @Test
  void deveLancarExcecaoSeJustificativaVazia() {
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("   ");

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, usuarioId));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertTrue(ex.getReason().toLowerCase().contains("justificativa"));
  }

  // ✅ Projeto já cancelado
  @Test
  void deveLancarExcecaoSeProjetoJaCancelado() {
    projeto.setStatus(StatusProjeto.CANCELADO);
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, usuarioId));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertTrue(ex.getReason().toLowerCase().contains("já está cancelado"));
  }

  // ✅ Projeto sem equipe executora
  @Test
  void deveLancarExcecaoSeProjetoSemEquipeExecutora() {
    projeto.setEquipeExecutora(null);
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, usuarioId));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertTrue(ex.getReason().toLowerCase().contains("equipe executora"));
  }

  // ✅ Usuário não é o responsável principal
  @Test
  void deveLancarExcecaoSeUsuarioNaoForResponsavelPrincipal() {
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> projetoService.cancelar(projetoId, dto, 999L));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    assertTrue(ex.getReason().toLowerCase().contains("responsável principal"));
  }

  // ✅ Atualização bem-sucedida
  @Test
  void deveAtualizarProjetoComSucesso() {
    Projeto projetoExistente = new Projeto();
    projetoExistente.setId(projetoId);
    projetoExistente.setTitulo("Antigo");

    ProjetoDTO dto = new ProjetoDTO();
    dto.setTitulo("Novo");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projetoExistente));
    doAnswer(
            invocation -> {
              ProjetoDTO source = invocation.getArgument(0);
              Projeto destino = invocation.getArgument(1);
              destino.setTitulo(source.getTitulo());
              return null;
            })
        .when(modelMapper)
        .map(any(ProjetoDTO.class), any(Projeto.class));
    when(projetoRepository.save(any(Projeto.class))).thenReturn(projetoExistente);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(dto);

    ProjetoDTO atualizado = projetoService.atualizarProjeto(projetoId, dto);

    assertNotNull(atualizado);
    assertEquals("Novo", atualizado.getTitulo());
  }

  // ✅ Atualização de projeto inexistente
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
