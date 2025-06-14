package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceImplTest {

  @InjectMocks private ProjetoServiceImpl projetoService;

  @Mock private ProjetoRepository projetoRepository;
  @Mock private ModelMapper modelMapper;
  @Mock private UsuarioRepository usuarioRepository;

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
}
