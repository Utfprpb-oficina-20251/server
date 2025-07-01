package br.edu.utfpr.pb.ext.server.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento genérico para entidades persistidas que precisam disparar notificações
 *
 * @param <T> Tipo da entidade associada ao evento
 */
@Getter
public class EntityEvent<T> extends ApplicationEvent {

  private final EventType eventType;

  public EntityEvent(T source, EventType eventType) {
    super(source);
    this.eventType = eventType;
  }

  @SuppressWarnings("unchecked")
  public T getEntity() {
    return (T) source;
  }

  public enum EventType {
    CREATED("Criação"),
    UPDATED("Atualização"),
    DELETED("Removido");

    final String displayName;

    EventType(String displayName) {
      this.displayName = displayName;
    }
  }
}
