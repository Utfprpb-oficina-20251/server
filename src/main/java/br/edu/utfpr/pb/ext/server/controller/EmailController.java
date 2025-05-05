package br.edu.utfpr.pb.ext.server.controller;

import br.edu.utfpr.pb.ext.server.service.EmailCodeValidationService;
import br.edu.utfpr.pb.ext.server.service.impl.EmailServiceImpl;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller para testar envio e validação de código por e-mail. */
@Tag(name = "Email", description = "API para envio e validação de códigos por email")
@RestController
@RequestMapping("/api/email")
public class EmailController {

  private final EmailServiceImpl emailService;
  private final EmailCodeValidationService validationService;

  public EmailController(EmailServiceImpl emailService,
                         EmailCodeValidationService validationService) {
    this.emailService = emailService;
    this.validationService = validationService;
  }
  @Operation(
    summary = "Envia código de verificação por email",
    description = "Gera um código aleatório e envia para o email informado com o tipo especificado"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Código enviado com sucesso"),
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos ou limite excedido"),
    @ApiResponse(responseCode = "500", description = "Erro ao enviar email")
  })
  @PostMapping("/enviar")
  public ResponseEntity<?> enviar(@RequestParam String email, @RequestParam String type)
      throws IOException {
    // Validação básica
    if (email == null || email.isBlank() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
      return ResponseEntity.badRequest().body("Email inválido");
    }
    if (type == null || type.isBlank()) {
      return ResponseEntity.badRequest().body("Tipo de código não informado");
    }

    emailService.generateAndSendCode(email, type);
    return ResponseEntity.ok(Map.of(
      "mensagem", "Código enviado com sucesso",
      "email", email,
      "tipo", type
    ));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<Map<String, String>> handleIOException(IOException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(Map.of("erro", "Falha ao enviar email: " + e.getMessage()));
  }

  @PostMapping("/validar")
  public ResponseEntity<Boolean> validar(
      @RequestParam String email, @RequestParam String type, @RequestParam String code) {
    boolean valido = validationService.validateCode(email, type, code);
    return ResponseEntity.ok(valido);
  }
}
