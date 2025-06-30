package br.edu.utfpr.pb.ext.server.event;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.pb.ext.server.candidatura.Candidatura;
import br.edu.utfpr.pb.ext.server.candidatura.StatusCandidatura;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CandidaturaEventTest {

  private Candidatura candidatura;

  @BeforeEach
  void setUp() {
    Usuario aluno = new Usuario();
    aluno.setId(1L);
    aluno.setNome("Aluno Teste");
    aluno.setEmail("aluno@email.com");

    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Teste");

    candidatura = new Candidatura();
    candidatura.setId(1L);
    candidatura.setAluno(aluno);
    candidatura.setProjeto(projeto);
    candidatura.setStatus(StatusCandidatura.PENDENTE);
  }

  @Test
  @DisplayName("Should create CandidaturaEvent with CREATED event type")
  void createCandidaturaEvent_withCreatedType_shouldSetCorrectProperties() {
    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);

    assertNotNull(event);
    assertEquals(candidatura, event.getEntity());
    assertEquals(EntityEvent.EventType.CREATED, event.getEventType());
  }

  @Test
  @DisplayName("Should create CandidaturaEvent with UPDATED event type")
  void createCandidaturaEvent_withUpdatedType_shouldSetCorrectProperties() {
    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.UPDATED);

    assertNotNull(event);
    assertEquals(candidatura, event.getEntity());
    assertEquals(EntityEvent.EventType.UPDATED, event.getEventType());
  }

  @Test
  @DisplayName("Should inherit from ApplicationEvent")
  void candidaturaEvent_shouldInheritFromApplicationEvent() {
    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);

    assertNotNull(event.getTimestamp());
    assertTrue(event.getTimestamp() > 0);
  }

  @Test
  @DisplayName("Should maintain entity reference")
  void candidaturaEvent_shouldMaintainEntityReference() {
    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);

    assertSame(candidatura, event.getEntity());
    assertEquals(1L, event.getEntity().getId());
    assertEquals("Aluno Teste", event.getEntity().getAluno().getNome());
    assertEquals("Projeto Teste", event.getEntity().getProjeto().getTitulo());
    assertEquals(StatusCandidatura.PENDENTE, event.getEntity().getStatus());
  }

  @Test
  @DisplayName("Should handle different event types correctly")
  void candidaturaEvent_withDifferentEventTypes_shouldSetCorrectType() {
    CandidaturaEvent createdEvent =
        new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);
    CandidaturaEvent updatedEvent =
        new CandidaturaEvent(candidatura, EntityEvent.EventType.UPDATED);

    assertEquals(EntityEvent.EventType.CREATED, createdEvent.getEventType());
    assertEquals(EntityEvent.EventType.UPDATED, updatedEvent.getEventType());
    assertNotEquals(createdEvent.getEventType(), updatedEvent.getEventType());
  }
}
