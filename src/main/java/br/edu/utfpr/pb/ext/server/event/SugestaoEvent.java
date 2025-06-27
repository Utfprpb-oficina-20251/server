package br.edu.utfpr.pb.ext.server.event;

import br.edu.utfpr.pb.ext.server.sugestaoprojeto.SugestaoDeProjeto;

public class SugestaoEvent extends EntityEvent<SugestaoDeProjeto> {
  public SugestaoEvent(SugestaoDeProjeto sugestao, EventType eventType) {
    super(sugestao, eventType);
  }
}
