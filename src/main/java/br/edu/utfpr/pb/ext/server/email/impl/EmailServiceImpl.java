package br.edu.utfpr.pb.ext.server.email.impl;

import br.edu.utfpr.pb.ext.server.email.EmailCode;
import br.edu.utfpr.pb.ext.server.email.EmailCodeRepository;
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

/** Serviço responsável por gerar códigos, enviar e-mails e registrar os dados no banco. */
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
   * Gera um código, envia para o e-mail e salva no banco.
   *
   * @param email destinatário
   * @param type tipo do código ("cadastro", "recuperacao", etc)
   * @return resposta do SendGrid
   * @throws IOException falha no envio
   */
  public Response generateAndSendCode(String email, String type) throws IOException {
    validarEmail(email);
    verificarLimiteEnvio(email, type);

    String code = gerarCodigoAleatorio();
    Response response = enviarEmailDeVerificacao(email, code, type);

    if (response.getStatusCode() == 202) {
      salvarCodigo(email, code, type);
      return response;
    }

    throw new IOException("Erro ao enviar e-mail. Código: " + response.getStatusCode());
  }

  // ======================
  // Métodos auxiliares
  // ======================

  private void validarEmail(String email) {
    if (email == null || !EMAIL_REGEX.matcher(email).matches()) {
      throw new IllegalArgumentException("Endereço de e-mail inválido.");
    }
  }

  private void verificarLimiteEnvio(String email, String type) {
    LocalDateTime inicio = LocalDateTime.now().minusHours(24);
    List<EmailCode> codigosRecentes =
        repository.findAllByEmailAndTypeAndGeneratedAtAfter(email, type, inicio);

    if (codigosRecentes.size() >= MAX_CODES_PER_DAY) {
      throw new IllegalArgumentException("Limite diário de envio atingido.");
    }
  }

  private String gerarCodigoAleatorio() {
    return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
  }

  private void salvarCodigo(String email, String code, String type) {
    EmailCode ec = new EmailCode();
    ec.setEmail(email);
    ec.setCode(code);
    ec.setType(type);
    ec.setGeneratedAt(LocalDateTime.now());
    ec.setExpiration(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
    ec.setUsed(false);
    repository.save(ec);
  }

  private Response enviarEmailDeVerificacao(String email, String code, String type)
      throws IOException {
    String assunto = "Código de Verificação - " + type;
    String mensagem =
        "Seu código é: " + code + "\n\nVálido por " + CODE_EXPIRATION_MINUTES + " minutos.";
    return enviarEmail(email, assunto, mensagem);
  }

  private Response enviarEmail(String destinatario, String assunto, String corpo)
      throws IOException {
    Email from = new Email("webprojeto2@gmail.com");
    Email to = new Email(destinatario);
    Content content = new Content("text/plain", corpo);
    Mail mail = new Mail(from, assunto, to, content);

    Request request = new Request();
    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());

    return sendGrid.api(request);
  }
}
