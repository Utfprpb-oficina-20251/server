package br.edu.utfpr.pb.ext.server.email;

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

/** Controller responsável pelo envio e validação de códigos por e-mail. */
@Tag(name = "Email", description = "API para envio e validação de códigos por e-mail")
@RestController
@RequestMapping("/api/email")
@Validated
public class EmailController {

  private final EmailServiceImpl emailService;
  private final EmailCodeValidationService validationService;

  /**
   * Cria uma instância do controlador de e-mail com os serviços necessários para envio e validação
   * de códigos.
   *
   * @param emailService serviço responsável por gerar e enviar códigos de verificação por e-mail
   * @param validationService serviço responsável por validar códigos de verificação recebidos
   */
  public EmailController(
      EmailServiceImpl emailService, EmailCodeValidationService validationService) {
    this.emailService = emailService;
    this.validationService = validationService;
  }

  /**
   * Gera e envia um código de verificação para o e-mail informado, associado ao tipo especificado.
   *
   * @param email endereço de e-mail que receberá o código de verificação
   * @param type tipo de operação para a qual o código será utilizado
   * @return resposta HTTP 200 com mensagem de sucesso, e-mail e tipo em caso de envio bem-sucedido
   * @throws IOException se ocorrer falha ao enviar o e-mail
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
    validarTipo(type);

    emailService.generateAndSendCode(email, type);

    return ResponseEntity.ok(
        Map.of(
            "mensagem", "Código enviado com sucesso",
            "email", email,
            "tipo", type));
  }

  /**
   * Valida um código de verificação enviado para um e-mail e tipo específicos.
   *
   * @param email endereço de e-mail para o qual o código foi enviado
   * @param type tipo de verificação associada ao código
   * @param code código de verificação a ser validado
   * @return ResponseEntity contendo um valor booleano que indica se o código é válido
   */
  @Operation(summary = "Valida o código de verificação enviado")
  @ApiResponse(responseCode = "200", description = "Validação realizada com sucesso")
  @PostMapping("/validar")
  public ResponseEntity<Boolean> validar(
      @RequestParam @NotBlank @Email String email,
      @RequestParam @NotBlank String type,
      @RequestParam @NotBlank String code) {
    boolean valido = validationService.validateCode(email, type, code);
    return ResponseEntity.ok(valido);
  }

  /**
   * Manipula exceções do tipo IllegalArgumentException lançadas durante o processamento das
   * requisições.
   *
   * <p>Retorna uma resposta HTTP 400 (Bad Request) com uma mensagem de erro em formato JSON.
   *
   * @param e exceção IllegalArgumentException capturada
   * @return resposta HTTP 400 contendo a mensagem de erro
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
  }

  /**
   * Manipula exceções de envio de e-mail, retornando resposta HTTP 500 com mensagem de erro.
   *
   * @param e exceção de E/S ocorrida durante o envio do e-mail
   * @return resposta contendo mensagem de erro em formato JSON e status 500
   */
  @ExceptionHandler(IOException.class)
  public ResponseEntity<Map<String, String>> handleIOException(IOException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("erro", "Falha ao enviar e-mail: " + e.getMessage()));
  }

  // ======================
  // Métodos auxiliares
  /**
   * Valida se o endereço de e-mail fornecido não é nulo, não está em branco e segue o formato
   * padrão de e-mail.
   *
   * @param email endereço de e-mail a ser validado
   * @throws IllegalArgumentException se o e-mail for nulo, estiver em branco ou não corresponder ao
   *     formato válido
   */
  private void validarEmail(String email) {
    if (email == null || email.isBlank() || !email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
      throw new IllegalArgumentException("Email inválido");
    }
  }

  /**
   * Valida se o parâmetro de tipo foi informado e não está em branco.
   *
   * @param type o tipo de código a ser validado
   * @throws IllegalArgumentException se o tipo for nulo ou estiver em branco
   */
  private void validarTipo(String type) {
    if (type == null || type.isBlank()) {
      throw new IllegalArgumentException("Tipo de código não informado");
    }
  }
}
