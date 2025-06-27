package br.edu.utfpr.pb.ext.server.event;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjetoEventTest {

  @Test
  @DisplayName("ProjetoEvent should store the project and event type correctly")
  void shouldStoreProjectAndEventType() {
    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Teste");

    ProjetoEvent event = new ProjetoEvent(projeto, EntityEvent.EventType.CREATED);

    assertNotNull(event);
    assertEquals(projeto, event.getEntity());
    assertEquals(EntityEvent.EventType.CREATED, event.getEventType());
  }
}
