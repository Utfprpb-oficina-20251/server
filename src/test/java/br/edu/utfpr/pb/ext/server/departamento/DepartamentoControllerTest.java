package br.edu.utfpr.pb.ext.server.departamento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários da camada de controle (controller) para o recurso Departamento.
 * Verifica o funcionamento correto dos endpoints da API exposta pelo DepartamentoController.
 */
class DepartamentoControllerTest {

    private DepartamentoService service;
    private DepartamentoController controller;

    /**
     * Configura o mock do serviço e instancia o controller antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        service = mock(DepartamentoService.class);
        controller = new DepartamentoController(service);
    }

    /**
     * Testa o endpoint GET /api/departamentos
     * Deve retornar uma lista com todos os departamentos existentes.
     */
    @Test
    void deveListarTodosOsDepartamentos() {
        Departamento departamento = new Departamento();
        departamento.setId(1L);
        departamento.setSigla("DINF");
        departamento.setNome("Departamento de Informática");

        when(service.findAll()).thenReturn(List.of(departamento));

        ResponseEntity<List<DepartamentoDto>> response = controller.listarTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("DINF", response.getBody().get(0).getSigla());
    }

    /**
     * Testa o endpoint GET /api/departamentos/{id}
     * Deve retornar o departamento correspondente ao ID.
     */
    @Test
    void deveBuscarDepartamentoPorId() {
        Departamento departamento = new Departamento();
        departamento.setId(1L);
        departamento.setSigla("DINF");
        departamento.setNome("Departamento de Informática");

        when(service.findOne(1L)).thenReturn(departamento);

        ResponseEntity<DepartamentoDto> response = controller.buscarPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("DINF", response.getBody().getSigla());
    }

    /**
     * Testa o endpoint POST /api/departamentos
     * Deve criar um novo departamento e retornar status 201 (CREATED).
     */
    @Test
    void deveCriarDepartamento() {
        DepartamentoDto dto = new DepartamentoDto();
        dto.setId(1L);
        dto.setSigla("DQUI");
        dto.setNome("Departamento de Química");

        Departamento entidade = new Departamento();
        entidade.setId(1L);
        entidade.setSigla("DQUI");
        entidade.setNome("Departamento de Química");

        when(service.save(any(Departamento.class))).thenReturn(entidade);

        ResponseEntity<DepartamentoDto> response = controller.criar(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("DQUI", response.getBody().getSigla());
    }

    /**
     * Testa o endpoint PUT /api/departamentos/{id}
     * Deve atualizar o departamento correspondente ao ID com sucesso.
     */
    @Test
    void deveAtualizarDepartamento() {
        DepartamentoDto dto = new DepartamentoDto();
        dto.setId(2L);
        dto.setSigla("DMEC");
        dto.setNome("Departamento de Mecânica");

        Departamento atualizado = new Departamento();
        atualizado.setId(2L);
        atualizado.setSigla("DMEC");
        atualizado.setNome("Departamento de Mecânica");

        when(service.save(any(Departamento.class))).thenReturn(atualizado);

        ResponseEntity<DepartamentoDto> response = controller.atualizar(2L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("DMEC", response.getBody().getSigla());
    }

    /**
     * Testa o endpoint PUT /api/departamentos/{id}
     * Não deve permitir a atualização se o ID da URL for diferente do ID do corpo.
     */
    @Test
    void naoDeveAtualizarComIdDivergente() {
        DepartamentoDto dto = new DepartamentoDto();
        dto.setId(99L); // diferente do ID informado na URL

        ResponseEntity<DepartamentoDto> response = controller.atualizar(1L, dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Testa o endpoint DELETE /api/departamentos/{id}
     * Deve excluir o departamento correspondente ao ID e retornar status 204 (NO CONTENT).
     */
    @Test
    void deveExcluirDepartamento() {
        doNothing().when(service).delete(1L);

        ResponseEntity<Void> response = controller.excluir(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service, times(1)).delete(1L);
    }
    /**
     * Testa o endpoint PUT /api/departamentos/{id}/responsavel/{usuarioId}
     * Deve associar corretamente um usuário como responsável pelo departamento.
     */
    @Test
    void deveAssociarResponsavelAoDepartamento() {
        Long departamentoId = 1L;
        Long usuarioId = 10L;

        // Simula o comportamento esperado
        doNothing().when(service).associarResponsavel(departamentoId, usuarioId);

        // Chama o endpoint do controller
        ResponseEntity<Void> response = controller.associarResponsavel(departamentoId, usuarioId);

        // Verifica o status e a chamada do método
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service, times(1)).associarResponsavel(departamentoId, usuarioId);
    }
}