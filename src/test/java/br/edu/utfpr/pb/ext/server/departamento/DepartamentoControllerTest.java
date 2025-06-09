package br.edu.utfpr.pb.ext.server.departamento;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

/**
 * Testes unitários para a classe DepartamentoController. Verifica o funcionamento dos principais
 * endpoints e lógica do controller.
 */
class DepartamentoControllerTest {

  private DepartamentoService service;
  private DepartamentoController controller;

  /** Configura o mock do service e instancia o controller antes de cada teste. */
  @BeforeEach
  void setUp() {
    service = mock(DepartamentoService.class);
    controller = new DepartamentoController(service, new ModelMapper());
  }

  /**
   * Testa o método associarResponsavel. Verifica se o método do serviço é chamado corretamente com
   * os IDs fornecidos.
   */
  @Test
  void deveAssociarResponsavelAoDepartamento() {
    // Arrange
    Long departamentoId = 1L;
    Long usuarioId = 10L;

    // Simula a chamada do método no service (void)
    doNothing().when(service).associarResponsavel(departamentoId, usuarioId);

    // Act
    controller.associarResponsavel(departamentoId, usuarioId);

    // Assert
    verify(service, times(1)).associarResponsavel(departamentoId, usuarioId);
  }

  /** Testa se o ModelMapper do controller não é nulo. */
  @Test
  void deveRetornarModelMapperNaoNulo() {
    assertNotNull(controller.getModelMapper());
  }

  /** Testa se o service retornado pelo controller é o mesmo mockado. */
  @Test
  void deveRetornarServiceMockado() {
    assertEquals(service, controller.getService());
  }
}
