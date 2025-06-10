package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.web.server.ResponseStatusException;

/**
 * Classe de testes unitários para {@link ProjetoServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class ProjetoServiceImplTest {

  @InjectMocks private ProjetoServiceImpl projetoService;

  @Mock private ProjetoRepository projetoRepository;
  @Mock private ModelMapper modelMapper;

  /**
   * Testa cancelamento válido feito pelo responsável principal.
   */
  @Test
  void deveCancelarProjetoQuandoResponsavelPrincipal() {
    Long projetoId = 1L;
    Long usuarioId = 100L;

    Usuario usuario = new Usuario();
    usuario.setId(usuarioId);

    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setEquipeExecutora(Collections.singletonList(usuario));
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Motivo válido");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    projetoService.cancelar(projetoId, dto, usuarioId);

    assertEquals(StatusProjeto.CANCELADO, projeto.getStatus());
    assertEquals("Motivo válido", projeto.getJustificativaCancelamento());
    verify(projetoRepository).save(projeto);
  }

  /**
   * Testa tentativa de cancelamento feita por usuário não responsável.
   */
  @Test
  void deveLancarExcecaoQuandoUsuarioNaoForResponsavelPrincipal() {
    Long projetoId = 1L;
    Long usuarioId = 100L;
    Long outroUsuarioId = 200L;

    Usuario usuario = new Usuario();
    usuario.setId(usuarioId);

    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setEquipeExecutora(Collections.singletonList(usuario));
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Motivo inválido");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, outroUsuarioId)
    );

    assertEquals(403, ex.getStatusCode().value());
    assertEquals("Apenas o responsável principal pode cancelar o projeto.", ex.getReason());
  }

  /**
   * Testa exceção ao tentar cancelar projeto inexistente.
   */
  @Test
  void deveLancarExcecaoQuandoProjetoNaoEncontrado() {
    Long projetoId = 1L;
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Motivo qualquer");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, 1L)
    );

    assertEquals(404, ex.getStatusCode().value());
    assertEquals("Projeto não encontrado", ex.getReason());
  }

  /**
   * Testa exceção ao tentar cancelar projeto já cancelado.
   */
  @Test
  void cancelar_quandoProjetoJaCancelado_deveLancarExcecao() {
    Long projetoId = 1L;
    Long usuarioId = 100L;

    Usuario usuario = new Usuario();
    usuario.setId(usuarioId);

    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setStatus(StatusProjeto.CANCELADO);
    projeto.setEquipeExecutora(Collections.singletonList(usuario));

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Já foi cancelado");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, usuarioId)
    );

    assertEquals(400, ex.getStatusCode().value());
    assertEquals("Projeto já está cancelado", ex.getReason());
  }

  /**
   * Testa exceção para equipe executora nula.
   */
  @Test
  void cancelar_quandoEquipeExecutoraForNula_deveLancarExcecao() {
    Long projetoId = 1L;
    Long usuarioId = 100L;

    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setEquipeExecutora(null);
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Tentando cancelar");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, usuarioId)
    );

    assertEquals(400, ex.getStatusCode().value());
    assertEquals("Projeto não possui equipe executora definida", ex.getReason());
  }

  /**
   * Testa exceção para equipe executora vazia.
   */
  @Test
  void cancelar_quandoEquipeExecutoraForVazia_deveLancarExcecao() {
    Long projetoId = 1L;
    Long usuarioId = 100L;

    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setEquipeExecutora(Collections.emptyList());
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Tentando cancelar");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, usuarioId)
    );

    assertEquals(400, ex.getStatusCode().value());
    assertEquals("Projeto não possui equipe executora definida", ex.getReason());
  }

  /**
   * Testa exceção para justificativa vazia.
   */
  @Test
  void cancelar_quandoJustificativaVazia_deveLancarExcecao() {
    Long projetoId = 1L;
    Long usuarioId = 100L;

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("   "); // espaço vazio

    ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, usuarioId)
    );

    assertEquals(400, ex.getStatusCode().value());
    assertEquals("A justificativa é obrigatória.", ex.getReason());
  }

  /**
   * Testa atualização bem-sucedida de projeto.
   */
  @Test
  void atualizarProjeto_quandoProjetoExiste_deveRetornarDTOAtualizado() {
    Long projetoId = 1L;

    ProjetoDTO dtoAtualizar = new ProjetoDTO();
    dtoAtualizar.setTitulo("Novo título");

    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setTitulo("Antigo");

    ProjetoDTO dtoEsperado = new ProjetoDTO();
    dtoEsperado.setId(projetoId);
    dtoEsperado.setTitulo("Novo título");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));
    when(projetoRepository.save(projeto)).thenReturn(projeto);
    when(modelMapper.map(projeto, ProjetoDTO.class)).thenReturn(dtoEsperado);

    ProjetoDTO resultado = projetoService.atualizarProjeto(projetoId, dtoAtualizar);

    assertNotNull(resultado);
    assertEquals(dtoEsperado.getTitulo(), resultado.getTitulo());
    verify(modelMapper).map(dtoAtualizar, projeto);
    verify(projetoRepository).save(projeto);
  }

  /**
   * Testa erro ao tentar atualizar projeto inexistente.
   */
  @Test
  void atualizarProjeto_quandoProjetoNaoExiste_deveLancarEntityNotFoundException() {
    Long projetoId = 99L;

    ProjetoDTO dto = new ProjetoDTO();
    when(projetoRepository.findById(projetoId)).thenReturn(Optional.empty());

    EntityNotFoundException ex = assertThrows(
            EntityNotFoundException.class,
            () -> projetoService.atualizarProjeto(projetoId, dto)
    );

    assertEquals("Projeto com ID 99 não encontrado.", ex.getMessage());
  }
}