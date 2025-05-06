package br.edu.utfpr.pb.ext.server.service.impl;

import br.edu.utfpr.pb.ext.server.model.EmailCode;
import br.edu.utfpr.pb.ext.server.repository.EmailCodeRepository;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** Serviço responsável por gerar códigos, enviar e-mails e registrar códigos no banco. */
@Service
public class EmailServiceImpl {

  private static final int CODE_EXPIRATION_MINUTES = 10;
  private static final int MAX_CODES_PER_DAY = 3;
  private static final Pattern EMAIL_REGEX = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

  private final EmailCodeRepository repository;
  private final SendGrid sendGrid;

  public EmailServiceImpl(EmailCodeRepository repository, SendGrid sendGrid) {
    this.repository = repository;
    this.sendGrid = sendGrid;
  }

  /**
   * Gera e envia um código de verificação por e-mail.
   *
   * @param email destinatário
   * @param type tipo do código ("cadastro", "recuperacao", etc)
   * @return resposta do SendGrid
   * @throws IOException falha no envio
   */
  public Response generateAndSendCode(String email, String type) throws IOException {
    if (!isValidEmail(email)) {
      throw new IllegalArgumentException("Endereço de e-mail inválido.");
    }

    if (hasExceededLimit(email, type)) {
      throw new IllegalArgumentException("Limite diário de envio atingido.");
    }

    String code = generateRandomCode();
    Response response = sendVerificationEmail(email, code, type);

    if (response.getStatusCode() == 202) {
      saveEmailCode(email, code, type);
    } else {
      throw new IOException("Erro ao enviar e-mail. Código: " + response.getStatusCode());
    }

    return response;
  }

  private boolean isValidEmail(String email) {
    return email != null && EMAIL_REGEX.matcher(email).matches();
  }

  private boolean hasExceededLimit(String email, String type) {
    LocalDateTime since = LocalDateTime.now().minusHours(24);
    List<EmailCode> recent =
        repository.findAllByEmailAndTypeAndGeneratedAtAfter(email, type, since);
    return recent.size() >= MAX_CODES_PER_DAY;
  }

  private String generateRandomCode() {
    return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
  }

  private void saveEmailCode(String email, String code, String type) {
    EmailCode ec = new EmailCode();
    ec.setEmail(email);
    ec.setCode(code);
    ec.setType(type);
    ec.setGeneratedAt(LocalDateTime.now());
    ec.setExpiration(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
    ec.setUsed(false);
    repository.save(ec);
  }

  private Response sendVerificationEmail(String email, String code, String type)
      throws IOException {
    String subject = "Código de Verificação - " + type;
    String content =
        "Seu código é: " + code + "\n\nVálido por " + CODE_EXPIRATION_MINUTES + " minutos.";
    return sendEmail(email, subject, content);
  }

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
