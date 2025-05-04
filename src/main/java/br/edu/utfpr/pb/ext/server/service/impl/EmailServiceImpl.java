package br.edu.utfpr.pb.ext.server.service.impl;

import br.edu.utfpr.pb.ext.server.model.EmailCode;
import br.edu.utfpr.pb.ext.server.repository.EmailCodeRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Serviço responsável por gerar códigos, enviar e-mails e registrar códigos no banco. */
@Service
public class EmailServiceImpl {

  // Tempo de expiração do código (em minutos)
  private static final int CODE_EXPIRATION_MINUTES = 10;

  // Limite de códigos por e-mail por dia
  private static final int MAX_CODES_PER_DAY = 3;

  @Autowired private EmailCodeRepository emailCodeRepository;

  @Autowired private SendGrid sendGrid;

  /**
   * Gera um código e envia para o e-mail informado com a finalidade informada.
   *
   * @param email E-mail destinatário
   * @param type Tipo do código ("cadastro", "recuperacao", etc.)
   * @return resposta da API SendGrid
   * @throws IOException em caso de falha no envio
   */
  public Response generateAndSendCode(String email, String type) throws IOException {
    // Valida limite de envio por e-mail nas últimas 24 horas
    LocalDateTime since = LocalDateTime.now().minusHours(24);
    List<EmailCode> recentCodes =
            emailCodeRepository.findAllByEmailAndTypeAndGeneratedAtAfter(email, type, since);

    if (recentCodes.size() >= MAX_CODES_PER_DAY) {
      throw new IllegalArgumentException(
              "Limite de códigos enviados para este e-mail nas últimas 24h.");
    }

    // Gera o código
    String code = generateRandomCode();

    // Monta o conteúdo do e-mail
    String subject = "Código de Verificação - " + type;
    String body =
            "Seu código de verificação é: "
                    + code
                    + "\n\n"
                    + "Este código é válido por "
                    + CODE_EXPIRATION_MINUTES
                    + " minutos.";

    // Envia o e-mail
    Response response = sendEmail(email, subject, body);

    if (response.getStatusCode() == 202) {
      // Salva no banco
      EmailCode emailCode = new EmailCode();
      emailCode.setEmail(email);
      emailCode.setCode(code);
      emailCode.setGeneratedAt(LocalDateTime.now());
      emailCode.setExpiration(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
      emailCode.setUsed(false);
      emailCode.setType(type);
      emailCodeRepository.save(emailCode);
    } else {
      throw new IOException(
              "Erro ao enviar e-mail via SendGrid. Código: " + response.getStatusCode());
    }

    return response;
  }

  /** Gera código aleatório de 6 caracteres em maiúsculas. */
  private String generateRandomCode() {
    return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
  }

  /** Envia e-mail simples com título e corpo. */
  private Response sendEmail(String email, String subject, String body) throws IOException {
    Email from = new Email("webprojeto2@gmail.com");
    Email to = new Email(email);
    Content content = new Content("text/plain", body);
    Mail mail = new Mail(from, subject, to, content);

    Request request = new Request();
    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());

    return sendGrid.api(request);
  }
}