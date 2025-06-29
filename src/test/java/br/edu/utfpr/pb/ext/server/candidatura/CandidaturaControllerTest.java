package br.edu.utfpr.pb.ext.server.candidatura;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class CandidaturaControllerTest {

  private ICandidaturaService candidaturaService;
  private CandidaturaController candidaturaController;

  @BeforeEach
  void setUp() {
    candidaturaService = mock(ICandidaturaService.class);
    candidaturaController = new CandidaturaController(candidaturaService);
  }

  @Test
  void candidatar_quandoUsuarioLogado_entaoRetornaCandidaturaDTO() {
    Long projetoId = 1L;

    CandidaturaDTO candidaturaDTO = new CandidaturaDTO();
    candidaturaDTO.setId(100L);

    when(candidaturaService.candidatar(projetoId)).thenReturn(candidaturaDTO);

    ResponseEntity<CandidaturaDTO> response = candidaturaController.candidatar(projetoId);

    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(100L, response.getBody().getId());
  }
}
