package br.edu.utfpr.pb.ext.server.candidatura;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.projeto.IProjetoService;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.IUsuarioService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.web.server.ResponseStatusException;

class CandidaturaServiceImplTest {

  private CandidaturaRepository candidaturaRepository;
  private IUsuarioService usuarioService;
  private IProjetoService projetoService;
  private ModelMapper modelMapper;

  private CandidaturaServiceImpl candidaturaService;

  @BeforeEach
  void setUp() {
    candidaturaRepository = mock(CandidaturaRepository.class);
    usuarioService = mock(IUsuarioService.class);
    projetoService = mock(IProjetoService.class);
    modelMapper = new ModelMapper();

    candidaturaService =
        new CandidaturaServiceImpl(
            candidaturaRepository, usuarioService, projetoService, modelMapper);
  }

  @Test
  void candidatar_quandoProjetoEmAndamento_eUsuarioValido_entaoSucesso() {
    Long projetoId = 1L;
    Long alunoId = 10L;

    Projeto projeto = Projeto.builder().status(StatusProjeto.EM_ANDAMENTO).qtdeVagas(5L).build();
    projeto.setId(projetoId);

    Usuario aluno = Usuario.builder().nome("Aluno Teste").build();
    aluno.setId(alunoId);

    when(projetoService.findOne(projetoId)).thenReturn(projeto);
    when(candidaturaRepository.existsByProjetoIdAndAlunoId(projetoId, alunoId)).thenReturn(false);
    when(candidaturaRepository.countByProjetoId(projetoId)).thenReturn(2L);
    when(usuarioService.findOne(alunoId)).thenReturn(aluno);

    Candidatura candidaturaSalva =
        Candidatura.builder()
            .aluno(aluno)
            .projeto(projeto)
            .dataCandidatura(LocalDateTime.now())
            .build();
    candidaturaSalva.setId(1L);

    when(candidaturaRepository.save(any(Candidatura.class))).thenReturn(candidaturaSalva);

    CandidaturaDTO dto = candidaturaService.candidatar(projetoId, alunoId);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
  }

  @Test
  void candidatar_quandoProjetoNaoEmAndamento_entaoBadRequest() {
    Projeto projeto = Projeto.builder().status(StatusProjeto.CANCELADO).build();
    projeto.setId(1L);

    when(projetoService.findOne(anyLong())).thenReturn(projeto);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> candidaturaService.candidatar(1L, 10L));

    assertEquals("400 BAD_REQUEST \"Projeto não está aberto para candidaturas\"", ex.getMessage());
  }

  @Test
  void candidatar_quandoJaInscrito_entaoBadRequest() {
    Projeto projeto = Projeto.builder().status(StatusProjeto.EM_ANDAMENTO).build();
    projeto.setId(1L);

    when(projetoService.findOne(anyLong())).thenReturn(projeto);
    when(candidaturaRepository.existsByProjetoIdAndAlunoId(anyLong(), anyLong())).thenReturn(true);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> candidaturaService.candidatar(1L, 10L));

    assertEquals("400 BAD_REQUEST \"Você já está inscrito neste projeto\"", ex.getMessage());
  }

  @Test
  void candidatar_quandoVagasPreenchidas_entaoBadRequest() {
    Projeto projeto = Projeto.builder().status(StatusProjeto.EM_ANDAMENTO).qtdeVagas(2L).build();
    projeto.setId(1L);

    when(projetoService.findOne(anyLong())).thenReturn(projeto);
    when(candidaturaRepository.existsByProjetoIdAndAlunoId(anyLong(), anyLong())).thenReturn(false);
    when(candidaturaRepository.countByProjetoId(anyLong())).thenReturn(2L);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> candidaturaService.candidatar(1L, 10L));

    assertEquals("400 BAD_REQUEST \"Vagas preenchidas\"", ex.getMessage());
  }
}
