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

  /**
   * Envia um código de verificação para o e-mail informado, de acordo com o tipo especificado.
   *
   * @param email endereço de e-mail para o qual o código será enviado
   * @param type tipo de operação relacionada ao código (por exemplo, cadastro, recuperação de senha)
   * @return resposta HTTP 200 com mensagem de confirmação do envio
   * @throws IOException se ocorrer um erro ao enviar o e-mail
   */
  @PostMapping("/enviar")
  public ResponseEntity<?> enviar(@RequestParam String email, @RequestParam String type)
      throws IOException {
    emailService.generateAndSendCode(email, type);
    return ResponseEntity.ok("Código enviado para " + email);
  }

  /**
   * Valida um código enviado por e-mail para um endereço e tipo específicos.
   *
   * @param email endereço de e-mail a ser validado
   * @param type tipo de operação relacionada ao código (por exemplo, cadastro, recuperação de senha)
   * @param code código recebido por e-mail para validação
   * @return ResponseEntity contendo um valor booleano que indica se o código é válido
   */
  @PostMapping("/validar")
  public ResponseEntity<Boolean> validar(
      @RequestParam String email, @RequestParam String type, @RequestParam String code) {
    boolean valido = validationService.validateCode(email, type, code);
    return ResponseEntity.ok(valido);
  }
}
