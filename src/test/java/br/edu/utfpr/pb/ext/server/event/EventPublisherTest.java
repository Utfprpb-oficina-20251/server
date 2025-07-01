package br.edu.utfpr.pb.ext.server.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import br.edu.utfpr.pb.ext.server.candidatura.Candidatura;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.SugestaoDeProjeto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

  @Mock private ApplicationEventPublisher publisher;

  @InjectMocks private EventPublisher eventPublisher;

  private Projeto projeto;
  private SugestaoDeProjeto sugestao;
  private Candidatura candidatura;

  @BeforeEach
  void setUp() {
    projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Teste");

    sugestao = new SugestaoDeProjeto();
    sugestao.setId(1L);
    sugestao.setTitulo("Sugestão Teste");

    candidatura = new Candidatura();
    candidatura.setId(1L);
  }

  @Test
  @DisplayName("Should publish ProjetoEvent with CREATED type")
  void publishProjetoCriado_shouldPublishCreatedEvent() {
    eventPublisher.publishProjetoCriado(projeto);
    verify(publisher).publishEvent(any(ProjetoEvent.class));
  }

  @Test
  @DisplayName("Should publish ProjetoEvent with UPDATED type")
  void publishProjetoAtualizado_shouldPublishUpdatedEvent() {
    eventPublisher.publishProjetoAtualizado(projeto);
    verify(publisher).publishEvent(any(ProjetoEvent.class));
  }

  @Test
  @DisplayName("Should publish SugestaoEvent with CREATED type")
  void publishSugestaoCriada_shouldPublishCreatedEvent() {
    eventPublisher.publishSugestaoCriada(sugestao);
    verify(publisher).publishEvent(any(SugestaoEvent.class));
  }

  @Test
  @DisplayName("Should publish SugestaoEvent with UPDATED type")
  void publishSugestaoAtualizada_shouldPublishUpdatedEvent() {
    eventPublisher.publishSugestaoAtualizada(sugestao);
    verify(publisher).publishEvent(any(SugestaoEvent.class));
  }

  @Test
  @DisplayName("Should publish CandidaturaEvent with CREATED type")
  void publishCandidaturaCriada_shouldPublishCreatedEvent() {
    eventPublisher.publishCandidaturaCriada(candidatura);
    verify(publisher).publishEvent(any(CandidaturaEvent.class));
  }

  @Test
  @DisplayName("Should publish CandidaturaEvent with UPDATED type")
  void publishCandidaturaAtualizada_shouldPublishUpdatedEvent() {
    eventPublisher.publishCandidaturaAtualizada(candidatura);
    verify(publisher).publishEvent(any(CandidaturaEvent.class));
  }
}
