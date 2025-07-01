package br.edu.utfpr.pb.ext.server.event;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.candidatura.Candidatura;
import br.edu.utfpr.pb.ext.server.candidatura.StatusCandidatura;
import br.edu.utfpr.pb.ext.server.email.impl.EmailServiceImpl;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.SugestaoDeProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

class EmailEventListenerTest {

  @Mock private EmailServiceImpl emailService;

  @Mock private TemplateEngine templateEngine;

  @InjectMocks private EmailEventListener emailEventListener;

  private Projeto projeto;
  private SugestaoDeProjeto sugestao;
  private Candidatura candidatura;
  private Usuario responsavel;
  private Usuario membroEquipe;
  private Usuario aluno;
  private Usuario professor;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    responsavel = new Usuario();
    responsavel.setEmail("responsavel@email.com");
    responsavel.setNome("Responsável Teste");

    membroEquipe = new Usuario();
    membroEquipe.setEmail("membro@email.com");

    aluno = new Usuario();
    aluno.setEmail("aluno@email.com");
    aluno.setNome("Aluno Teste");

    professor = new Usuario();
    professor.setEmail("professor@email.com");

    projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Teste");
    projeto.setResponsavel(responsavel);
    projeto.setEquipeExecutora(List.of(membroEquipe));

    sugestao = new SugestaoDeProjeto();
    sugestao.setId(1L);
    sugestao.setTitulo("Sugestão Teste");
    sugestao.setAluno(aluno);
    sugestao.setProfessor(professor);

    candidatura = new Candidatura();
    candidatura.setId(1L);
    candidatura.setAluno(aluno);
    candidatura.setProjeto(projeto);
    candidatura.setStatus(StatusCandidatura.PENDENTE);

    when(templateEngine.process(anyString(), any(Context.class))).thenReturn("conteudo-email");
  }

  @Test
  @DisplayName("Should send email to responsavel and equipe when project is created")
  void handleProjetoEvent_created_sendsEmail() throws Exception {
    ProjetoEvent event = new ProjetoEvent(projeto, EntityEvent.EventType.CREATED);

    emailEventListener.handleProjetoEvent(event);

    verify(emailService, times(1))
        .sendEmail(
            eq("responsavel@email.com"),
            contains("Novo projeto criado"),
            eq("conteudo-email"),
            eq("text/html"));
    verify(emailService, times(1))
        .sendEmail(
            eq("membro@email.com"),
            contains("Novo projeto criado"),
            eq("conteudo-email"),
            eq("text/html"));
  }

  @Test
  @DisplayName("Should send email to responsavel and equipe when project is updated")
  void handleProjetoEvent_updated_sendsEmail() throws Exception {
    ProjetoEvent event = new ProjetoEvent(projeto, EntityEvent.EventType.UPDATED);

    emailEventListener.handleProjetoEvent(event);

    verify(emailService, times(1))
        .sendEmail(
            eq("responsavel@email.com"),
            contains("Projeto atualizado"),
            eq("conteudo-email"),
            eq("text/html"));
    verify(emailService, times(1))
        .sendEmail(
            eq("membro@email.com"),
            contains("Projeto atualizado"),
            eq("conteudo-email"),
            eq("text/html"));
  }

  @Test
  @DisplayName("Should send email to aluno and professor when suggestion is created")
  void handleSugestaoEvent_created_sendsEmail() throws Exception {
    SugestaoEvent event = new SugestaoEvent(sugestao, EntityEvent.EventType.CREATED);

    emailEventListener.handleSugestaoEvent(event);

    verify(emailService, times(1))
        .sendEmail(
            eq("aluno@email.com"),
            contains("Nova sugestão registrada"),
            eq("conteudo-email"),
            eq("text/html"));
    verify(emailService, times(1))
        .sendEmail(
            eq("professor@email.com"),
            contains("Nova sugestão registrada"),
            eq("conteudo-email"),
            eq("text/html"));
  }

  @Test
  @DisplayName("Should send email to aluno and professor when suggestion is updated")
  void handleSugestaoEvent_updated_sendsEmail() throws Exception {
    SugestaoEvent event = new SugestaoEvent(sugestao, EntityEvent.EventType.UPDATED);

    emailEventListener.handleSugestaoEvent(event);

    verify(emailService, times(1))
        .sendEmail(
            eq("aluno@email.com"),
            contains("Sugestão atualizada"),
            eq("conteudo-email"),
            eq("text/html"));
    verify(emailService, times(1))
        .sendEmail(
            eq("professor@email.com"),
            contains("Sugestão atualizada"),
            eq("conteudo-email"),
            eq("text/html"));
  }

  @Test
  @DisplayName("Should send email to aluno and responsavel when candidatura is created")
  void handleCandidaturaEvent_created_sendsEmail() throws Exception {
    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);

    emailEventListener.handleCandidaturaEvent(event);

    verify(emailService, times(1))
        .sendEmail(
            eq("aluno@email.com"),
            contains("Nova candidatura recebida"),
            eq("conteudo-email"),
            eq("text/html"));
    verify(emailService, times(1))
        .sendEmail(
            eq("responsavel@email.com"),
            contains("Nova candidatura recebida"),
            eq("conteudo-email"),
            eq("text/html"));
  }

  @Test
  @DisplayName("Should send email to aluno and responsavel when candidatura is updated")
  void handleCandidaturaEvent_updated_sendsEmail() throws Exception {
    candidatura.setStatus(StatusCandidatura.APROVADA);
    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.UPDATED);

    emailEventListener.handleCandidaturaEvent(event);

    verify(emailService, times(1))
        .sendEmail(
            eq("aluno@email.com"),
            contains("Status da candidatura atualizado"),
            eq("conteudo-email"),
            eq("text/html"));
    verify(emailService, times(1))
        .sendEmail(
            eq("responsavel@email.com"),
            contains("Status da candidatura atualizado"),
            eq("conteudo-email"),
            eq("text/html"));
  }

  @Test
  @DisplayName("Should not send email if no recipients for candidatura")
  void handleCandidaturaEvent_noRecipients_doesNotSendEmail() throws Exception {
    candidatura.setAluno(null);
    candidatura.setProjeto(null);
    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);

    emailEventListener.handleCandidaturaEvent(event);

    verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  @DisplayName("Should send email only to aluno when responsavel has no email")
  void handleCandidaturaEvent_responsavelNoEmail_sendsOnlyToAluno() throws Exception {
    responsavel.setEmail(null);
    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);

    emailEventListener.handleCandidaturaEvent(event);

    verify(emailService, times(1))
        .sendEmail(
            eq("aluno@email.com"),
            contains("Nova candidatura recebida"),
            eq("conteudo-email"),
            eq("text/html"));
    verify(emailService, never())
        .sendEmail(eq("responsavel@email.com"), anyString(), anyString(), anyString());
  }

  @Test
  @DisplayName("Should send email only to responsavel when aluno has no email")
  void handleCandidaturaEvent_alunoNoEmail_sendsOnlyToResponsavel() throws Exception {
    aluno.setEmail(null);
    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);

    emailEventListener.handleCandidaturaEvent(event);

    verify(emailService, times(1))
        .sendEmail(
            eq("responsavel@email.com"),
            contains("Nova candidatura recebida"),
            eq("conteudo-email"),
            eq("text/html"));
    verify(emailService, never())
        .sendEmail(eq("aluno@email.com"), anyString(), anyString(), anyString());
  }

  @Test
  @DisplayName("Should not send email if no recipients for project")
  void handleProjetoEvent_noRecipients_doesNotSendEmail() throws Exception {
    projeto.setResponsavel(null);
    projeto.setEquipeExecutora(null);
    ProjetoEvent event = new ProjetoEvent(projeto, EntityEvent.EventType.CREATED);

    emailEventListener.handleProjetoEvent(event);

    verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  @DisplayName("Should not send email if no recipients for suggestion")
  void handleSugestaoEvent_noRecipients_doesNotSendEmail() throws Exception {
    sugestao.setAluno(null);
    sugestao.setProfessor(null);
    SugestaoEvent event = new SugestaoEvent(sugestao, EntityEvent.EventType.CREATED);

    emailEventListener.handleSugestaoEvent(event);

    verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyString());
  }
}
