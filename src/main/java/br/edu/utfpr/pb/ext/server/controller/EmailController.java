package br.edu.utfpr.pb.ext.server.controller;

import br.edu.utfpr.pb.ext.server.service.EmailCodeValidationService;
import br.edu.utfpr.pb.ext.server.service.impl.EmailServiceImpl;
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

/**
 * Controller responsável pelo envio e validação de códigos por e-mail.
 *
 * <p>Fornece endpoints para: - Enviar um código de verificação para o e-mail informado. - Validar
 * um código previamente enviado.
 */
@Tag(name = "Email", description = "API para envio e validação de códigos por e-mail")
@RestController
@RequestMapping("/api/email")
@Validated
public class EmailController {

  private final EmailServiceImpl emailService;
  private final EmailCodeValidationService validationService;

  /**
   * Construtor com injeção de dependência.
   *
   * @param emailService Serviço responsável pela geração e envio de código.
   * @param validationService Serviço responsável pela validação de código.
   */
  public EmailController(
      EmailServiceImpl emailService, EmailCodeValidationService validationService) {
    this.emailService = emailService;
    this.validationService = validationService;
  }

  /**
   * Endpoint para envio de código de verificação para o e-mail informado.
   *
   * @param email E-mail destinatário (obrigatório e válido).
   * @param type Tipo do código (ex: "cadastro", "recuperacao").
   * @return ResponseEntity com mensagem de sucesso.
   * @throws IOException Exceção em caso de falha no envio via provedor.
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
  public ResponseEntity<?> enviar(@RequestParam String email, @RequestParam String type)
      throws IOException {

    // Validações manuais (necessárias para cobertura de testes e segurança extra)
    if (email == null || email.isBlank() || !email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
      throw new IllegalArgumentException("Email inválido");
    }

    if (type == null || type.isBlank()) {
      throw new IllegalArgumentException("Tipo de código não informado");
    }

    emailService.generateAndSendCode(email, type);

    return ResponseEntity.ok(
        Map.of(
            "mensagem", "Código enviado com sucesso",
            "email", email,
            "tipo", type));
  }

  /**
   * Endpoint para validar o código enviado anteriormente ao e-mail informado.
   *
   * @param email E-mail destinatário.
   * @param type Tipo do código.
   * @param code Código a ser validado.
   * @return true se o código for válido, false caso contrário.
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
   * Tratamento para exceções de parâmetros inválidos ou limite excedido.
   *
   * @param e Exceção lançada.
   * @return ResponseEntity com status 400 e mensagem de erro.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
  }

  /**
   * Tratamento para falhas no envio de e-mail.
   *
   * @param e Exceção de IO.
   * @return ResponseEntity com status 500 e mensagem de erro.
   */
  @ExceptionHandler(IOException.class)
  public ResponseEntity<Map<String, String>> handleIOException(IOException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("erro", "Falha ao enviar e-mail: " + e.getMessage()));
  }
}
