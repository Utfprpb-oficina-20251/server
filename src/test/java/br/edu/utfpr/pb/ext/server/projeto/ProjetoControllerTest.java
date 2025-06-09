package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioProjetoDTO;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
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

  @Test
  void shouldCreateProjetoSuccessfully() {
    // Arrange
    ProjetoDTO projetoDTO = new ProjetoDTO();
    projetoDTO.setTitulo("Projeto Teste");
    projetoDTO.setEquipeExecutora(
        List.of(UsuarioProjetoDTO.builder().nome("batata").email("batata@utfpr.edu.br").build()));

    Usuario usuario = new Usuario();
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

    when(projetoService.atualizarProjeto(eq(projetoId), any(ProjetoDTO.class))).thenReturn(dtoResponse);

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
}
