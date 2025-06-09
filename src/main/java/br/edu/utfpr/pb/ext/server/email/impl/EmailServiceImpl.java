package br.edu.utfpr.pb.ext.server.email.impl;

import br.edu.utfpr.pb.ext.server.email.EmailCode;
import br.edu.utfpr.pb.ext.server.email.EmailCodeRepository;
import br.edu.utfpr.pb.ext.server.email.enums.TipoDeNotificacao;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Serviço responsável por gerar códigos, enviar e-mails e registrar os dados no banco. */
@Service
public class EmailServiceImpl {

  private static final int CODE_EXPIRATION_MINUTES = 10;
  private static final int MAX_CODES_PER_DAY = 30;
  private static final int MAX_CODES_IN_SHORT_PERIOD = 5;
  private static final int SHORT_PERIOD_REST_IN_MINUTES = 15;
  private static final Pattern EMAIL_REGEX = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
  public static final String ERRO_LIMITE_DIARIO =
      "Quantidade de solicitações ultrapassa o limite das últimas 24 horas.";
  public static final String ERRO_LIMITE_CURTO =
      "Limite de solicitações atingido, tente novamente em %d minutos.";
  private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

  private final EmailCodeRepository repository;
  private final SendGrid sendGrid;

  /**
   * Cria uma instância do serviço de e-mail com o repositório de códigos e o cliente SendGrid
   * fornecidos.
   */
  public EmailServiceImpl(EmailCodeRepository repository, SendGrid sendGrid) {
    this.repository = repository;
    this.sendGrid = sendGrid;
  }

  /**
   * Gera e envia um código de verificação para o e-mail informado, registrando o código no banco de
   * dados.
   *
   * <p>Valida o tipo e o formato do e-mail, verifica o limite diário de envios, gera um código
   * aleatório, envia o e-mail de verificação e salva o código caso o envio seja aceito.
   *
   * @param email endereço de e-mail do destinatário
   * @param type tipo do código de verificação (ex: "cadastro", "recuperacao")
   * @return resposta da API do SendGrid referente ao envio do e-mail
   * @throws IllegalArgumentException se o tipo for nulo, vazio, se o e-mail for inválido ou se o
   *     limite diário de envios for excedido
   * @throws IOException se o envio do e-mail falhar
   */
  public Response generateAndSendCode(String email, String type) throws IOException {
    if (type == null || type.isBlank()) {
      throw new IllegalArgumentException("O tipo do código é obrigatório.");
    }
    validarEmail(email);
    verificarLimiteEnvio(email, type);

    String code = gerarCodigoAleatorio();
    Response response = enviarEmailDeVerificacao(email, code, type);
    salvarCodigo(email, code, type);
    return response;
  }

  // ======================
  // Métodos auxiliares
  /**
   * Valida se o endereço de e-mail fornecido está no formato correto.
   *
   * @param email endereço de e-mail a ser validado
   * @throws IllegalArgumentException se o e-mail for nulo ou não corresponder ao padrão esperado
   */
  private void validarEmail(String email) {
    if (email == null || !EMAIL_REGEX.matcher(email).matches()) {
      throw new IllegalArgumentException("Endereço de e-mail inválido.");
    }
  }

  /**
   * Verifica se o limite diário de envio de códigos para o e-mail e tipo especificados foi
   * atingido.
   *
   * <p>Lança uma exceção se o número de códigos enviados nas últimas 24 horas for igual ou superior
   * ao permitido.
   *
   * @param email endereço de e-mail a ser verificado
   * @param type tipo de código relacionado ao envio
   * @throws IllegalArgumentException se o limite diário de envio for atingido
   */
  private void verificarLimiteEnvio(String email, String type) {
    LocalDateTime limiteDiario = LocalDateTime.now().minusHours(24);
    Long codigos = repository.countByEmailAndTypeAndGeneratedAtAfter(email, type, limiteDiario);
    if (codigos > MAX_CODES_PER_DAY) {
      throw new IllegalArgumentException(ERRO_LIMITE_DIARIO);
    }

    LocalDateTime limiteCurto = LocalDateTime.now().minusMinutes(SHORT_PERIOD_REST_IN_MINUTES);
    codigos = repository.countByEmailAndTypeAndGeneratedAtAfter(email, type, limiteCurto);

    if (codigos > MAX_CODES_IN_SHORT_PERIOD) {
      throw new IllegalArgumentException(ERRO_LIMITE_CURTO.formatted(SHORT_PERIOD_REST_IN_MINUTES));
    }
  }

  /**
   * Gera um código aleatório de 4 caracteres alfanuméricos em maiúsculas.
   *
   * @return código aleatório de 4 caracteres alfanuméricos
   */
  private String gerarCodigoAleatorio() {
    return UUID.randomUUID().toString().substring(0, 4).toUpperCase();
  }

  /**
   * Salva um novo código de verificação de e-mail no banco de dados, incluindo informações de
   * validade e status de uso.
   *
   * @param email endereço de e-mail associado ao código
   * @param code código de verificação gerado
   * @param type tipo do código ou finalidade do envio
   */
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

  /**
   * Envia um e-mail de verificação contendo um código para o endereço especificado.
   *
   * <p>O e-mail inclui o código de verificação e informa o tempo de validade em minutos.
   *
   * @param email endereço de e-mail do destinatário
   * @param code código de verificação a ser enviado
   * @param type tipo de verificação associado ao código
   * @return resposta da API do SendGrid após o envio do e-mail
   * @throws IOException se ocorrer falha ao enviar o e-mail
   */
  private Response enviarEmailDeVerificacao(String email, String code, String type)
      throws IOException {
    String assunto = "Código de Verificação - " + type;
    String mensagem =
        "Seu código é: " + code + "\n\nVálido por " + CODE_EXPIRATION_MINUTES + " minutos.";
    return sendEmail(email, assunto, mensagem, "text/plain");
  }

  /**
   * Envia um e-mail de notificação com conteúdo HTML para o destinatário informado, de acordo com o
   * tipo de notificação e dados do projeto.
   *
   * <p>Dependendo do tipo de notificação, o assunto e a mensagem do e-mail são personalizados para
   * informar sobre inscrição de aluno, notificação ao professor ou atualização de status de
   * sugestão de projeto.
   *
   * @param email endereço de e-mail do destinatário
   * @param tipo tipo de notificação a ser enviada
   * @param projeto nome do projeto relacionado à notificação
   * @param link URL relevante ao projeto ou ação notificada
   * @return resposta da API do SendGrid após o envio do e-mail
   * @throws IllegalArgumentException se algum parâmetro for nulo ou vazio
   * @throws IOException se ocorrer falha no envio do e-mail
   */
  public Response enviarEmailDeNotificacao(
      String email, TipoDeNotificacao tipo, String projeto, String link) throws IOException {

    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email não pode ser nulo ou vazio.");
    }
    if (tipo == null) {
      throw new IllegalArgumentException("Tipo de notificação não pode ser nulo.");
    }
    if (projeto == null || projeto.isBlank()) {
      throw new IllegalArgumentException("Projeto não pode ser nulo ou vazio.");
    }
    if (link == null || link.isBlank()) {
      throw new IllegalArgumentException("Link não pode ser nulo ou vazio.");
    }

    String assunto = "";
    String mensagemHtml = "";

    switch (tipo) {
      case INSCRICAO_ALUNO -> {
        assunto = "Inscrição no projeto: " + projeto;
        mensagemHtml = montarMensagem("Você se cadastrou com sucesso no projeto", projeto, link);
      }
      case INSCRICAO_ALUNO_PROFESSOR -> {
        assunto = "Inscrição no projeto: " + projeto;
        mensagemHtml = montarMensagem("Um aluno acabou de se cadastrar no projeto", projeto, link);
      }
      case ATUALIZACAO_STATUS -> {
        assunto = "Atualização no status da sugestão de projeto: " + projeto;
        mensagemHtml =
            montarMensagem(
                "Houve uma atualização no status da sua sugestão de projeto", projeto, link);
      }
    }
    return sendEmail(email, assunto, mensagemHtml, "text/html");
  }

  /**
   * Monta uma mensagem HTML personalizada para notificação por e-mail.
   *
   * @param conteudo texto principal da mensagem
   * @param projeto nome do projeto relacionado à notificação
   * @param link URL para o usuário acessar mais informações
   * @return mensagem formatada em HTML pronta para envio por e-mail
   */
  private String montarMensagem(String conteudo, String projeto, String link) {
    return String.format(
        "<h1>Olá!</h1><p>%s: <strong>%s</strong></p><a href=\"%s\">Conferir</a>",
        conteudo, projeto, link);
  }

  /**
   * Envia um e-mail utilizando o serviço SendGrid.
   *
   * @param to endereço de e-mail do destinatário
   * @param subject assunto do e-mail
   * @param contentText conteúdo do e-mail (texto ou HTML)
   * @param tipo tipo do conteúdo do e-mail, como "text/plain" ou "text/html"
   * @return o objeto Response do SendGrid em caso de envio bem-sucedido
   * @throws IOException se o envio do e-mail falhar ou retornar status diferente de 202
   */
  public Response sendEmail(String to, String subject, String contentText, String tipo)
      throws IOException {
    Email from = new Email("webprojeto2@gmail.com");
    Email toEmail = new Email(to);
    Content content = new Content(tipo, contentText);
    Mail mail = new Mail(from, subject, toEmail, content);

    Request request = new Request();
    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());

    Response response = sendGrid.api(request);

    if (response == null || response.getStatusCode() != 202) {
      logger.error(
          "Erro ao enviar e-mail por sendgrid, status code: {}",
          response != null ? response.getStatusCode() : 0);
      throw new IOException("Erro ao tentar enviar e-mail, tente novamente mais tarde");
    }

    return response;
  }
}
