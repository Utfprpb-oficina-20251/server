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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Serviço responsável por gerar códigos, enviar e-mails e registrar códigos no banco. */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

  // Tempo de expiração do código (em minutos)
  private static final int CODE_EXPIRATION_MINUTES = 10;

  // Limite de códigos por e-mail por dia
  private static final int MAX_CODES_PER_DAY = 3;

  private final EmailCodeRepository emailCodeRepository;

  private final SendGrid sendGrid;

  /**
   * Gera e envia um código de verificação para o e-mail informado, respeitando o limite diário de
   * envios.
   *
   * <p>Caso o limite de 3 códigos por e-mail e tipo nas últimas 24 horas seja excedido, lança uma
   * exceção. O código gerado é salvo no banco de dados com informações de expiração e uso.
   *
   * @param email endereço de e-mail do destinatário
   * @param type finalidade do código (por exemplo, "cadastro" ou "recuperacao")
   * @return resposta da API SendGrid referente ao envio do e-mail
   * @throws IOException se houver falha no envio do e-mail via SendGrid
   * @throws IllegalArgumentException se o limite diário de códigos for excedido para o e-mail e
   *     tipo informados
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

  /**
   * Gera um código de verificação aleatório de 6 caracteres em letras maiúsculas.
   *
   * @return código de verificação de 6 caracteres
   */
  private String generateRandomCode() {
    return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
  }

  /**
   * Envia um e-mail simples para o destinatário especificado com o assunto e corpo fornecidos,
   * utilizando a API SendGrid.
   *
   * @param email endereço de e-mail do destinatário
   * @param subject assunto do e-mail
   * @param body corpo do e-mail em texto simples
   * @return resposta da API SendGrid referente ao envio do e-mail
   * @throws IOException se ocorrer uma falha ao se comunicar com a API SendGrid
   */
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
