package br.edu.utfpr.pb.ext.server.event;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.pb.ext.server.sugestaoprojeto.SugestaoDeProjeto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SugestaoEventTest {

  @Test
  @DisplayName("SugestaoEvent should store the suggestion and event type correctly")
  void shouldStoreSuggestionAndEventType() {
    SugestaoDeProjeto sugestao = new SugestaoDeProjeto();
    sugestao.setId(1L);
    sugestao.setTitulo("Sugestão Teste");

    SugestaoEvent event = new SugestaoEvent(sugestao, EntityEvent.EventType.CREATED);

    assertNotNull(event);
    assertEquals(sugestao, event.getEntity());
    assertEquals(EntityEvent.EventType.CREATED, event.getEventType());
  }
}
