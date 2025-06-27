package br.edu.utfpr.pb.ext.server.event;

import br.edu.utfpr.pb.ext.server.projeto.Projeto;

public class ProjetoEvent extends EntityEvent<Projeto> {
  public ProjetoEvent(Projeto projeto, EventType eventType) {
    super(projeto, eventType);
  }
}
