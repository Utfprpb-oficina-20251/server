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
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Serviço responsável por gerar códigos, enviar e-mails e registrar códigos no banco. */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

  private static final int CODE_EXPIRATION_MINUTES = 10; // Tempo de expiração do código
  private static final int MAX_CODES_PER_DAY = 3; // Limite de códigos por e-mail por dia
  private static final Pattern EMAIL_REGEX = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

  private final EmailCodeRepository emailCodeRepository;
  private final SendGrid sendGrid;

  /**
   * Gera e envia um código de verificação para o e-mail informado, respeitando o limite diário de
   * envios. Quebra em funções auxiliares para validar, gerar, enviar e persistir o código.
   *
   * @param email endereço de e-mail do destinatário
   * @param type finalidade do código (por exemplo, "cadastro" ou "recuperacao")
   * @return resposta da API SendGrid referente ao envio do e-mail
   * @throws IOException se houver falha no envio do e-mail via SendGrid
   */
  public Response generateAndSendCode(String email, String type) throws IOException {
    if (!isValidEmail(email)) {
      throw new IllegalArgumentException("Endereço de e-mail inválido.");
    }

    if (hasExceededLimit(email, type)) {
      throw new IllegalArgumentException(
          "Limite de códigos enviados para este e-mail nas últimas 24h.");
    }

    String code = generateRandomCode();
    Response response = sendVerificationEmail(email, code, type);

    if (response.getStatusCode() == 202) {
      saveEmailCode(email, code, type);
    } else {
      throw new IOException(
          "Erro ao enviar e-mail via SendGrid. Código: " + response.getStatusCode());
    }

    return response;
  }

  /** Valida o formato do e-mail com regex. */
  private boolean isValidEmail(String email) {
    return email != null && EMAIL_REGEX.matcher(email).matches();
  }

  /** Verifica se o limite de envio diário foi excedido. */
  private boolean hasExceededLimit(String email, String type) {
    LocalDateTime since = LocalDateTime.now().minusHours(24);
    List<EmailCode> recentCodes =
        emailCodeRepository.findAllByEmailAndTypeAndGeneratedAtAfter(email, type, since);
    return recentCodes.size() >= MAX_CODES_PER_DAY;
  }

  /** Gera um código aleatório de 6 caracteres em letras maiúsculas. */
  private String generateRandomCode() {
    return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
  }

  /** Salva o código gerado no banco de dados. */
  private void saveEmailCode(String email, String code, String type) {
    EmailCode emailCode = new EmailCode();
    emailCode.setEmail(email);
    emailCode.setCode(code);
    emailCode.setGeneratedAt(LocalDateTime.now());
    emailCode.setExpiration(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
    emailCode.setUsed(false);
    emailCode.setType(type);
    emailCodeRepository.save(emailCode);
  }

  /** Monta e envia o e-mail com o código. */
  private Response sendVerificationEmail(String email, String code, String type)
      throws IOException {
    String subject = "Código de Verificação - " + type;
    String body =
        "Seu código de verificação é: "
            + code
            + "\n\nEste código é válido por "
            + CODE_EXPIRATION_MINUTES
            + " minutos.";

    return sendEmail(email, subject, body);
  }

  /** Envia o e-mail simples via SendGrid. */
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
