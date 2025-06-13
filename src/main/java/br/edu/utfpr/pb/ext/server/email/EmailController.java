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
    TipoCodigo tipoCodigo = converterParaTipoCodigo(type);
    validarTipo(tipoCodigo);

    emailService.generateAndSendCode(email, tipoCodigo);

    return ResponseEntity.ok(
        Map.of(
            "mensagem", "Código enviado com sucesso", "email", email, "tipo", tipoCodigo.name()));
  }

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

  private void validarEmail(String email) {
    if (email == null || email.isBlank() || !email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
      throw new IllegalArgumentException("Email inválido");
    }
  }

  private void validarTipo(TipoCodigo type) {
    if (type == null) {
      throw new IllegalArgumentException("Tipo de código não informado");
    }
  }

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
