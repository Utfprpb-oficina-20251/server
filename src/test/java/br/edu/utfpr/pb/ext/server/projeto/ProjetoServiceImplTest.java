package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

// Ativa a integração do Mockito com o JUnit 5
@ExtendWith(MockitoExtension.class)
class ProjetoServiceImplTest {

    // Cria uma instância real do service e injeta os mocks abaixo nela
    @InjectMocks
    private ProjetoServiceImpl projetoService;

    // Cria mocks para as dependências do service
    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private ModelMapper modelMapper;

    /**
     * Testa o cenário de sucesso da atualização.
     * Garante que o projeto é encontrado, mapeado, salvo e retornado como DTO.
     */
    @Test
    void atualizarProjeto_quandoProjetoExiste_deveRetornarDTOAtualizado() {
        // Arrange (Organização)
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

        // Assert (Verificação)
        assertNotNull(resultado);
        assertEquals(dtoEsperado.getId(), resultado.getId());
        assertEquals(dtoEsperado.getTitulo(), resultado.getTitulo());

        // Verifica se os métodos dos mocks foram chamados como esperado
        verify(projetoRepository).findById(projetoId);
        verify(modelMapper).map(dadosParaAtualizar, projetoOriginal);
        verify(projetoRepository).save(projetoOriginal);
        verify(modelMapper).map(projetoOriginal, ProjetoDTO.class);
    }

    /**
     * Testa o cenário de falha quando o projeto a ser atualizado não existe.
     * Garante que uma EntityNotFoundException é lançada.
     */
    @Test
    void atualizarProjeto_quandoProjetoNaoExiste_deveLancarEntityNotFoundException() {
        // Arrange (Organização)
        Long idInexistente = 99L;
        ProjetoDTO dadosParaAtualizar = new ProjetoDTO(); // DTO qualquer
        String mensagemErro = "Projeto com ID " + idInexistente + " não encontrado.";

        when(projetoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert (Ação e Verificação)
        EntityNotFoundException exception =
                assertThrows(
                        EntityNotFoundException.class,
                        () -> projetoService.atualizarProjeto(idInexistente, dadosParaAtualizar));

        assertEquals(mensagemErro, exception.getMessage());

        verify(projetoRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }
}