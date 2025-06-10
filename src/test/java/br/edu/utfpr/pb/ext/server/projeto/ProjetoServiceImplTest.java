package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
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

  @InjectMocks
  private ProjetoServiceImpl projetoService;

  @Mock
  private ProjetoRepository projetoRepository;

  @Mock
  private ModelMapper modelMapper;

  private Projeto projeto;

  @BeforeEach
  void setUp() {
    projeto = new Projeto();
    projeto.setId(1L);
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
    projeto.setEquipeExecutora(Collections.singletonList(criarUsuario(10L)));
  }

  private Usuario criarUsuario(Long id) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    return usuario;
  }

  @Test
  void deveCancelarProjeto_QuandoUsuarioEhResponsavelPrincipal() {
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Motivo válido");

    when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
    projetoService.cancelar(1L, dto, 10L);

    assertEquals(StatusProjeto.CANCELADO, projeto.getStatus());
    assertEquals("Motivo válido", projeto.getJustificativaCancelamento());
    verify(projetoRepository).save(projeto);
  }

  @Test
  void deveLancarErro_QuandoJustificativaForNula() {
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa(null);

    ResponseStatusException exception =
            assertThrows(ResponseStatusException.class, () -> projetoService.cancelar(1L, dto, 10L));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getReason().contains("justificativa"));
  }

  @Test
  void deveLancarErro_QuandoProjetoNaoForEncontrado() {
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa");

    when(projetoRepository.findById(1L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
            assertThrows(ResponseStatusException.class, () -> projetoService.cancelar(1L, dto, 10L));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void deveLancarErro_QuandoProjetoJaEstiverCancelado() {
    projeto.setStatus(StatusProjeto.CANCELADO);
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa");

    when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

    ResponseStatusException exception =
            assertThrows(ResponseStatusException.class, () -> projetoService.cancelar(1L, dto, 10L));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getReason().contains("já está cancelado"));
  }

  @Test
  void deveLancarErro_QuandoProjetoNaoTemEquipeExecutora() {
    projeto.setEquipeExecutora(null);
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa");

    when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

    ResponseStatusException exception =
            assertThrows(ResponseStatusException.class, () -> projetoService.cancelar(1L, dto, 10L));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getReason().contains("equipe executora"));
  }

  @Test
  void deveLancarErro_QuandoUsuarioNaoForResponsavelPrincipal() {
    CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
    dto.setJustificativa("Justificativa");

    when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

    ResponseStatusException exception =
            assertThrows(ResponseStatusException.class, () -> projetoService.cancelar(1L, dto, 99L));

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertTrue(exception.getReason().contains("responsável principal"));
  }
}