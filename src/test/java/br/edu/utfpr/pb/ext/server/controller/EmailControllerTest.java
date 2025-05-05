package br.edu.utfpr.pb.ext.server.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.service.EmailCodeValidationService;
import br.edu.utfpr.pb.ext.server.service.impl.EmailServiceImpl;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/** Testes unitários para EmailController, verificando os retornos de envio e validação. */
@ExtendWith(MockitoExtension.class)
class EmailControllerTest {

  @Mock private EmailServiceImpl emailService;

  @Mock private EmailCodeValidationService validationService;

  @InjectMocks private EmailController controller;

  @BeforeEach
  void setUp() {
    // Nada necessário por enquanto
  }

  /** Teste de envio bem-sucedido. */
  @Test
  void testEnviar_Success() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";

    when(emailService.generateAndSendCode(email, tipo)).thenReturn(null);

    ResponseEntity<?> response = controller.enviar(email, tipo);

    assertEquals(200, response.getStatusCodeValue());
    assertTrue(response.getBody().toString().contains("Código enviado com sucesso"));
  }

  /** Teste de validação de código verdadeiro. */
  @Test
  void testValidar_Success() {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";
    String codigo = "ABC123";

    when(validationService.validateCode(email, tipo, codigo)).thenReturn(true);

    ResponseEntity<Boolean> response = controller.validar(email, tipo, codigo);

    assertEquals(200, response.getStatusCodeValue());
    assertTrue(response.getBody());
  }

  /** Teste de validação de código falso. */
  @Test
  void testValidar_CodigoInvalido() {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";
    String codigo = "XYZ999";

    when(validationService.validateCode(email, tipo, codigo)).thenReturn(false);

    ResponseEntity<Boolean> response = controller.validar(email, tipo, codigo);

    assertEquals(200, response.getStatusCodeValue());
    assertFalse(response.getBody());
  }

  /** Teste de exceção IllegalArgumentException (ex: limite atingido). */
  @Test
  void testEnviar_IllegalArgumentException() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";

    when(emailService.generateAndSendCode(email, tipo))
        .thenThrow(new IllegalArgumentException("Limite atingido"));

    ResponseEntity<?> response = null;
    try {
      controller.enviar(email, tipo);
      fail("Deveria ter lançado IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      response = controller.handleIllegalArgumentException(e);
    }

    assertNotNull(response);
    assertEquals(400, response.getStatusCodeValue());
    assertTrue(response.getBody().toString().contains("Limite atingido"));
  }

  /** Teste de exceção IOException (ex: erro na API SendGrid). */
  @Test
  void testEnviar_IOException() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";

    when(emailService.generateAndSendCode(email, tipo))
        .thenThrow(new IOException("Erro na API SendGrid"));

    ResponseEntity<?> response = null;
    try {
      controller.enviar(email, tipo);
      fail("Deveria ter lançado IOException");
    } catch (IOException e) {
      response = controller.handleIOException(e);
    }

    assertNotNull(response);
    assertEquals(500, response.getStatusCodeValue());
    assertTrue(response.getBody().toString().contains("Falha ao enviar email"));
  }

  /** Teste para e-mail inválido (regex falha). */
  @Test
  void testEnviar_EmailInvalido() throws IOException {
    String email = "email-invalido";
    String tipo = "cadastro";

    ResponseEntity<?> response = controller.enviar(email, tipo);

    assertEquals(400, response.getStatusCodeValue());
    assertTrue(response.getBody().toString().contains("Email inválido"));
  }

  /** Teste para tipo de código vazio. */
  @Test
  void testEnviar_TipoVazio() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "";

    ResponseEntity<?> response = controller.enviar(email, tipo);

    assertEquals(400, response.getStatusCodeValue());
    assertTrue(response.getBody().toString().contains("Tipo de código não informado"));
  }

  /** Teste para e-mail nulo. */
  @Test
  void testEnviar_EmailNulo() throws IOException {
    String tipo = "cadastro";

    ResponseEntity<?> response = controller.enviar(null, tipo);

    assertEquals(400, response.getStatusCodeValue());
    assertTrue(response.getBody().toString().contains("Email inválido"));
  }

  /** Teste para tipo nulo. */
  @Test
  void testEnviar_TipoNulo() throws IOException {
    String email = "teste@utfpr.edu.br";

    ResponseEntity<?> response = controller.enviar(email, null);

    assertEquals(400, response.getStatusCodeValue());
    assertTrue(response.getBody().toString().contains("Tipo de código não informado"));
  }
}
