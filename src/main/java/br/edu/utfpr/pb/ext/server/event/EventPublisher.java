package br.edu.utfpr.pb.ext.server.event;

import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.SugestaoDeProjeto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {

  private final ApplicationEventPublisher publisher;

  public void publishProjetoCriado(Projeto projeto) {
    publisher.publishEvent(new ProjetoEvent(projeto, EntityEvent.EventType.CREATED));
  }

  public void publishProjetoAtualizado(Projeto projeto) {
    publisher.publishEvent(new ProjetoEvent(projeto, EntityEvent.EventType.UPDATED));
  }

  public void publishSugestaoCriada(SugestaoDeProjeto sugestao) {
    publisher.publishEvent(new SugestaoEvent(sugestao, EntityEvent.EventType.CREATED));
  }

  public void publishSugestaoAtualizada(SugestaoDeProjeto sugestao) {
    publisher.publishEvent(new SugestaoEvent(sugestao, EntityEvent.EventType.UPDATED));
  }
}
