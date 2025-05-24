package br.edu.utfpr.pb.ext.server.email;

import br.edu.utfpr.pb.ext.server.email.enums.TipoDeNotificacao;
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

  public EmailController(
      EmailServiceImpl emailService, EmailCodeValidationService validationService) {
    this.emailService = emailService;
    this.validationService = validationService;
  }

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

//EndPoint para teste de notificação
@Operation(summary = "Endpoint para teste de envio de email, retirar na versão final")
@ApiResponse(responseCode = "200", description = "envio realizado com sucesso")
@PostMapping("/teste/{email}")
public ResponseEntity<String> teste(@PathVariable("email") String email) {
    try {
        emailService.enviarEmailDeNotificacao(email,TipoDeNotificacao.ATUALIZACAO_STATUS,"PROJETO TESTE", "google.com");

        return ResponseEntity.ok("E-mail enviado com sucesso!");
    } catch (IOException e) {
        return  ResponseEntity.internalServerError().body("Erro ao enviar e-mail: " + e.getMessage());
    }
}
 //------------
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<Map<String, String>> handleIOException(IOException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("erro", "Falha ao enviar e-mail: " + e.getMessage()));
  }

  // ======================
  // Métodos auxiliares
  // ======================

  private void validarEmail(String email) {
    if (email == null || email.isBlank() || !email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
      throw new IllegalArgumentException("Email inválido");
    }
  }

  private void validarTipo(String type) {
    if (type == null || type.isBlank()) {
      throw new IllegalArgumentException("Tipo de código não informado");
    }
  }
}
