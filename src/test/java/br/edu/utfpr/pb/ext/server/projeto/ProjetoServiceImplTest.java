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

@ExtendWith(MockitoExtension.class)
class ProjetoServiceImplTest {

  @InjectMocks private ProjetoServiceImpl projetoService;

  @Mock private ProjetoRepository projetoRepository;

  @Mock private ModelMapper modelMapper;

  @BeforeEach
  void setupMocks() {
    // Você pode configurar aqui se precisar alterar mocks dinamicamente
  }

  @Test
  void deveCancelarProjetoQuandoResponsavelPrincipal() {
    Long projetoId = 1L;
    Long usuarioId = 100L;
    Usuario usuario = new Usuario();
    usuario.setId(usuarioId);

    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setEquipeExecutora(Collections.singletonList(usuario));

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa válida");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    projetoService.cancelar(projetoId, dto, usuarioId);

    assertEquals(StatusProjeto.CANCELADO, projeto.getStatus());
    assertEquals("Justificativa válida", projeto.getJustificativaCancelamento());
    verify(projetoRepository).save(projeto);
  }

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

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Tentativa inválida");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, outroUsuarioId));

    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void deveLancarExcecaoQuandoProjetoNaoEncontrado() {
    Long projetoId = 1L;
    Long usuarioId = 100L;

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Qualquer");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.empty());

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, usuarioId));

    assertEquals(404, ex.getStatusCode().value());
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
    dto.setJustificativa("Tentativa de cancelar projeto já cancelado");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, usuarioId)
    );

    assertEquals(400, ex.getStatusCode().value());
    assertEquals("Projeto já está cancelado", ex.getReason());
  }
  @Test
  void cancelar_quandoEquipeExecutoraForNula_deveLancarExcecao() {
    Long projetoId = 1L;
    Long usuarioId = 100L;

    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
    projeto.setEquipeExecutora(null); // ← Aqui o ponto

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa válida");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, usuarioId)
    );

    assertEquals(400, ex.getStatusCode().value());
    assertEquals("Projeto não possui equipe executora definida", ex.getReason());
  }
  @Test
  void cancelar_quandoEquipeExecutoraForVazia_deveLancarExcecao() {
    Long projetoId = 1L;
    Long usuarioId = 100L;

    Projeto projeto = new Projeto();
    projeto.setId(projetoId);
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
    projeto.setEquipeExecutora(Collections.emptyList()); // ← Equipe vazia

    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Tentativa com equipe vazia");

    when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));

    ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> projetoService.cancelar(projetoId, dto, usuarioId)
    );

    assertEquals(400, ex.getStatusCode().value());
    assertEquals("Projeto não possui equipe executora definida", ex.getReason());
  }
}
