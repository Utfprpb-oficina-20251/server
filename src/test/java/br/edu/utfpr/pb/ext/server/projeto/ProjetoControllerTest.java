package br.edu.utfpr.pb.ext.server.projeto;


import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioProjetoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoControllerTest {

    @InjectMocks
    private ProjetoController projetoController;

    @Mock
    private IProjetoService projetoService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ModelMapper modelMapper;

    @Test
    void shouldCreateProjetoSuccessfully() {
        // Arrange
        ProjetoDTO projetoDTO = new ProjetoDTO();
        projetoDTO.setTitulo("Projeto Teste");
        new UsuarioProjetoDTO();
        projetoDTO.setEquipeExecutora(List.of(UsuarioProjetoDTO.builder()
                .nomeCompleto("batata")
                .emailInstitucional("batata@utfpr.edu.br")
                .build()));

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
    }
}
