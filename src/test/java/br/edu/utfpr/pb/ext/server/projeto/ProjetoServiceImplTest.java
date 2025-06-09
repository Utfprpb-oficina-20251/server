package br.edu.utfpr.pb.ext.server.projeto;


import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.projeto.ProjetoRepository;
import br.edu.utfpr.pb.ext.server.projeto.CancelamentoProjetoDTO;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjetoServiceImplTest {

    private ProjetoRepository projetoRepository;
    private ProjetoServiceImpl projetoService;

    @BeforeEach
    void setUp() {
        projetoRepository = mock(ProjetoRepository.class);
        projetoService = new ProjetoServiceImpl(projetoRepository);
    }

    @Test
    void deveCancelarProjetoQuandoResponsavelPrincipal() {
        // Arrange
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

        // Act
        projetoService.cancelar(projetoId, dto, usuarioId);

        // Assert
        assertEquals(StatusProjeto.CANCELADO, projeto.getStatus());
        assertEquals("Justificativa válida", projeto.getJustificativaCancelamento());
        verify(projetoRepository).save(projeto);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoForResponsavelPrincipal() {
        // Arrange
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

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                projetoService.cancelar(projetoId, dto, outroUsuarioId)
        );

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void deveLancarExcecaoQuandoProjetoNaoEncontrado() {
        // Arrange
        Long projetoId = 1L;
        Long usuarioId = 100L;

        CancelamentoProjetoDTO dto = new CancelamentoProjetoDTO();
        dto.setJustificativa("Qualquer");

        when(projetoRepository.findById(projetoId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                projetoService.cancelar(projetoId, dto, usuarioId)
        );

        assertEquals(404, ex.getStatusCode().value());
    }
}