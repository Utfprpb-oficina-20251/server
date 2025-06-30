package br.edu.utfpr.pb.ext.server.event;

import br.edu.utfpr.pb.ext.server.candidatura.Candidatura;
import br.edu.utfpr.pb.ext.server.email.impl.EmailServiceImpl;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.SugestaoDeProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailEventListener {

  private final EmailServiceImpl emailService;
  private final TemplateEngine templateEngine;

  @Async
  @EventListener
  public void handleProjetoEvent(ProjetoEvent event) {
    Projeto projeto = event.getEntity();
    List<String> recipients = new ArrayList<>();

    // Add responsavel if exists
    if (projeto.getResponsavel() != null && projeto.getResponsavel().getEmail() != null) {
      recipients.add(projeto.getResponsavel().getEmail());
    }

    // Add all team members
    if (projeto.getEquipeExecutora() != null) {
      projeto.getEquipeExecutora().stream()
          .filter(usuario -> usuario != null && usuario.getEmail() != null)
          .map(Usuario::getEmail)
          .forEach(recipients::add);
    }

    if (recipients.isEmpty()) {
      log.warn("Nenhum destinatário encontrado para o projeto: {}", projeto.getTitulo());
      return;
    }

    Context context = new Context();
    context.setVariable("entityTitle", projeto.getTitulo());
    context.setVariable("dataHora", LocalDateTime.now());

    switch (event.getEventType()) {
      case CREATED -> {
        context.setVariable("titulo", "Novo Projeto Criado");
        context.setVariable("subtitulo", "Um projeto foi criado com sucesso");
        context.setVariable("mensagem", "O projeto foi registrado com sucesso no sistema.");
        sendTemplateEmailToMultipleRecipients(
            recipients,
            "Novo projeto criado: " + projeto.getTitulo(),
            "notification-template",
            context);
      }
      case UPDATED -> {
        context.setVariable("titulo", "Projeto Atualizado");
        context.setVariable("subtitulo", "Um projeto foi atualizado");
        context.setVariable("mensagem", "As alterações no projeto foram salvas com sucesso.");
        sendTemplateEmailToMultipleRecipients(
            recipients,
            "Projeto atualizado: " + projeto.getTitulo(),
            "notification-template",
            context);
      }
    }
  }

  @Async
  @EventListener
  public void handleSugestaoEvent(SugestaoEvent event) {
    SugestaoDeProjeto sugestao = event.getEntity();
    List<String> recipients = new ArrayList<>();

    // Add aluno if exists
    if (sugestao.getAluno() != null && sugestao.getAluno().getEmail() != null) {
      recipients.add(sugestao.getAluno().getEmail());
    }

    // Add professor if exists
    if (sugestao.getProfessor() != null && sugestao.getProfessor().getEmail() != null) {
      recipients.add(sugestao.getProfessor().getEmail());
    }

    if (recipients.isEmpty()) {
      log.warn("Nenhum destinatário encontrado para a sugestão: {}", sugestao.getTitulo());
      return;
    }

    Context context = new Context();
    context.setVariable("entityTitle", sugestao.getTitulo());
    context.setVariable("dataHora", LocalDateTime.now());

    switch (event.getEventType()) {
      case CREATED -> {
        context.setVariable("titulo", "Nova Sugestão Registrada");
        context.setVariable("subtitulo", "Uma sugestão foi registrada com sucesso");
        context.setVariable("mensagem", "A sugestão foi compartilhada com sucesso no sistema.");
        sendTemplateEmailToMultipleRecipients(
            recipients,
            "Nova sugestão registrada: " + sugestao.getTitulo(),
            "notification-template",
            context);
      }
      case UPDATED -> {
        context.setVariable("titulo", "Sugestão Atualizada");
        context.setVariable("subtitulo", "Uma sugestão foi atualizada");
        context.setVariable("mensagem", "As alterações na sugestão foram salvas com sucesso.");
        sendTemplateEmailToMultipleRecipients(
            recipients,
            "Sugestão atualizada: " + sugestao.getTitulo(),
            "notification-template",
            context);
      }
    }
  }

  @Async
  @EventListener
  public void handleCandidaturaEvent(CandidaturaEvent event) {
    Candidatura candidatura = event.getEntity();
    List<String> recipients = new ArrayList<>();

    if (candidatura.getAluno() != null && candidatura.getAluno().getEmail() != null) {
      recipients.add(candidatura.getAluno().getEmail());
    }

    if (candidatura.getProjeto() != null
        && candidatura.getProjeto().getResponsavel() != null
        && candidatura.getProjeto().getResponsavel().getEmail() != null) {
      recipients.add(candidatura.getProjeto().getResponsavel().getEmail());
    }

    if (recipients.isEmpty()) {
      log.warn(
          "Nenhum destinatário encontrado para a candidatura do projeto: {}",
          candidatura.getProjeto() != null ? candidatura.getProjeto().getTitulo() : "N/A");
      return;
    }

    Context context = new Context();
    context.setVariable(
        "entityTitle",
        candidatura.getProjeto() != null ? candidatura.getProjeto().getTitulo() : "Projeto");
    context.setVariable(
        "nomeAluno", candidatura.getAluno() != null ? candidatura.getAluno().getNome() : "N/A");
    context.setVariable("statusCandidatura", candidatura.getStatus());
    context.setVariable("dataHora", LocalDateTime.now());

    switch (event.getEventType()) {
      case CREATED -> {
        String subject =
            "Nova candidatura recebida - "
                + (candidatura.getProjeto() != null
                    ? candidatura.getProjeto().getTitulo()
                    : "Projeto");
        sendTemplateEmailToMultipleRecipients(recipients, subject, "candidatura-created", context);
        log.info("Email de nova candidatura enviado para: {}", recipients);
      }
      case UPDATED -> {
        String subject =
            "Status da candidatura atualizado - "
                + (candidatura.getProjeto() != null
                    ? candidatura.getProjeto().getTitulo()
                    : "Projeto");
        sendTemplateEmailToMultipleRecipients(recipients, subject, "candidatura-updated", context);
        log.info("Email de atualização de candidatura enviado para: {}", recipients);
      }
    }
  }

  private void sendTemplateEmailToMultipleRecipients(
      List<String> recipients, String subject, String templateName, Context context) {
    String content = templateEngine.process(templateName, context);

    for (String recipient : recipients) {
      try {
        emailService.sendEmail(recipient, subject, content, "text/html");
        log.info("Email enviado para {} com assunto: {}", recipient, subject);
      } catch (Exception e) {
        log.error("Erro ao enviar email para {}: {}", recipient, e.getMessage(), e);
      }
    }
  }
}
