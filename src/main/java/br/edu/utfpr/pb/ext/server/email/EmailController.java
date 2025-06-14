package br.edu.utfpr.pb.ext.server.email;

import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import br.edu.utfpr.pb.ext.server.email.impl.EmailServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Email", description = "API para envio e validação de códigos por e-mail")
@RestController
@RequestMapping("/api/email")
@Validated
public class EmailController {

  private final EmailServiceImpl emailService;
  private final EmailCodeValidationService validationService;

  /**
   * Cria uma instância do controlador de e-mail com os serviços necessários para envio e validação de códigos.
   */
  public EmailController(
      EmailServiceImpl emailService, EmailCodeValidationService validationService) {
    this.emailService = emailService;
    this.validationService = validationService;
  }

  /**
   * Envia um código de verificação para o e-mail informado, de acordo com o tipo especificado.
   *
   * Valida o formato do e-mail e o tipo de código antes de gerar e enviar o código de verificação.
   *
   * @param email endereço de e-mail do destinatário
   * @param type tipo de código de verificação (deve corresponder a um valor válido do enum TipoCodigo)
   * @return resposta contendo mensagem de sucesso, e-mail e tipo de código enviado
   * @throws IllegalArgumentException se o e-mail ou o tipo forem inválidos
   * @throws IOException se ocorrer erro ao enviar o e-mail
   */
  @Operation(
      summary = "Envia código de verificação por e-mail",
      description =
          "Gera um código aleatório e envia para o e-mail informado com o tipo especificado")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Código enviado com sucesso"),
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos ou limite excedido"),
    @ApiResponse(responseCode = "500", description = "Erro ao enviar e-mail")
  })
  @PostMapping("/enviar")
  public ResponseEntity<Map<String, String>> enviar(
      @RequestParam String email, @RequestParam String type) throws IOException {
    validarEmail(email);
    TipoCodigo tipoCodigo = converterParaTipoCodigo(type);
    validarTipo(tipoCodigo);

    emailService.generateAndSendCode(email, tipoCodigo);

    return ResponseEntity.ok(
        Map.of(
            "mensagem", "Código enviado com sucesso", "email", email, "tipo", tipoCodigo.name()));
  }

  /**
   * Valida um código de verificação enviado para o e-mail informado.
   *
   * @param email endereço de e-mail a ser validado
   * @param type tipo do código de verificação
   * @param code código de verificação recebido pelo usuário
   * @return true se o código for válido para o e-mail e tipo informados, false caso contrário
   */
  @Operation(summary = "Valida o código de verificação enviado")
  @ApiResponse(responseCode = "200", description = "Validação realizada com sucesso")
  @PostMapping("/validar")
  public ResponseEntity<Boolean> validar(
      @RequestParam @NotBlank @Email String email,
      @RequestParam @NotBlank String type,
      @RequestParam @NotBlank String code) {
    TipoCodigo tipoCodigo = converterParaTipoCodigo(type);
    boolean valido = validationService.validateCode(email, tipoCodigo, code);
    return ResponseEntity.ok(valido);
  }

  /**
   * Manipula exceções do tipo IllegalArgumentException lançadas pelos endpoints, retornando uma resposta HTTP 400 com a mensagem de erro.
   *
   * @param e exceção IllegalArgumentException capturada
   * @return resposta HTTP 400 contendo a mensagem de erro no corpo
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
  }

  /**
   * Manipula exceções de envio de e-mail, retornando uma resposta HTTP 500 com mensagem de erro.
   *
   * @return resposta contendo a mensagem de erro detalhada sobre a falha no envio do e-mail
   */
  @ExceptionHandler(IOException.class)
  public ResponseEntity<Map<String, String>> handleIOException(IOException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("erro", "Falha ao enviar e-mail: " + e.getMessage()));
  }

  /**
   * Valida se o endereço de e-mail fornecido não é nulo, não está em branco e segue o formato padrão de e-mail.
   *
   * @param email endereço de e-mail a ser validado
   * @throws IllegalArgumentException se o e-mail for nulo, estiver em branco ou não corresponder ao formato válido
   */
  private void validarEmail(String email) {
    if (email == null || email.isBlank() || !email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
      throw new IllegalArgumentException("Email inválido");
    }
  }

  /**
   * Valida se o tipo de código foi informado.
   *
   * @param type o tipo de código a ser validado
   * @throws IllegalArgumentException se o tipo de código for nulo
   */
  private void validarTipo(TipoCodigo type) {
    if (type == null) {
      throw new IllegalArgumentException("Tipo de código não informado");
    }
  }

  /**
   * Converte uma string para o enum {@link TipoCodigo}.
   *
   * @param type valor em string representando o tipo de código
   * @return o valor correspondente do enum {@link TipoCodigo}
   * @throws IllegalArgumentException se o parâmetro for nulo, vazio ou não corresponder a um valor válido do enum
   */
  private TipoCodigo converterParaTipoCodigo(String type) {
    if (type == null || type.isBlank()) {
      throw new IllegalArgumentException("Tipo de código não informado");
    }

    try {
      return TipoCodigo.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Tipo de código inválido: " + type);
    }
  }
}
