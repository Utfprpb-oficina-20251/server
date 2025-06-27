package br.edu.utfpr.pb.ext.server.event;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EntityEventTest {

  @Test
  @DisplayName("Should store and return the entity and event type correctly")
  void testEntityEventStoresEntityAndEventType() {
    String entity = "TestEntity";
    EntityEvent<String> event = new EntityEvent<>(entity, EntityEvent.EventType.CREATED);

    assertEquals(entity, event.getEntity());
    assertEquals(EntityEvent.EventType.CREATED, event.getEventType());
    assertEquals(entity, event.getSource());
  }

  @Test
  @DisplayName("EventType enum should have correct display names")
  void testEventTypeDisplayNames() {
    assertEquals("Criação", EntityEvent.EventType.CREATED.displayName);
    assertEquals("Atualização", EntityEvent.EventType.UPDATED.displayName);
    assertEquals("Removido", EntityEvent.EventType.DELETED.displayName);
  }
}
