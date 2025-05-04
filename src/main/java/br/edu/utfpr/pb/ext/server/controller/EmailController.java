package br.edu.utfpr.pb.ext.server.controller;

import br.edu.utfpr.pb.ext.server.service.EmailCodeValidationService;
import br.edu.utfpr.pb.ext.server.service.impl.EmailServiceImpl;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller para testar envio e validação de código por e-mail. */
@RestController
@RequestMapping("/api/email")
public class EmailController {

  @Autowired private EmailServiceImpl emailService;

  @Autowired private EmailCodeValidationService validationService;

  @PostMapping("/enviar")
  public ResponseEntity<?> enviar(@RequestParam String email, @RequestParam String type)
      throws IOException {
    emailService.generateAndSendCode(email, type);
    return ResponseEntity.ok("Código enviado para " + email);
  }

  @PostMapping("/validar")
  public ResponseEntity<Boolean> validar(
      @RequestParam String email, @RequestParam String type, @RequestParam String code) {
    boolean valido = validationService.validateCode(email, type, code);
    return ResponseEntity.ok(valido);
  }
}
