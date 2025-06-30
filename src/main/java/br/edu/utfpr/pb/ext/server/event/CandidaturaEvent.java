package br.edu.utfpr.pb.ext.server.event;

import br.edu.utfpr.pb.ext.server.candidatura.Candidatura;

public class CandidaturaEvent extends EntityEvent<Candidatura> {
  public CandidaturaEvent(Candidatura candidatura, EventType eventType) {
    super(candidatura, eventType);
  }
}
