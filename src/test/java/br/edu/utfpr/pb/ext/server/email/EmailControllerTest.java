package br.edu.utfpr.pb.ext.server.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.email.impl.EmailServiceImpl;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Testes unitários para EmailController, verificando os retornos de envio e validação. */
@ExtendWith(MockitoExtension.class)
class EmailControllerTest {

  @Mock private EmailServiceImpl emailService;

  @Mock private EmailCodeValidationService validationService;

  @InjectMocks private EmailController controller;

  @BeforeEach
  void setUp() {}

  /** Teste de envio bem-sucedido. */
  @Test
  void testEnviar_Success() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";

    when(emailService.generateAndSendCode(email, tipo)).thenReturn(null);

    ResponseEntity<?> response = controller.enviar(email, tipo);

    assertEquals(HttpStatus.OK, response.getStatusCode());
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

    assertEquals(HttpStatus.OK, response.getStatusCode());
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

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody());
  }

  /** Teste de exceção IllegalArgumentException (ex: limite atingido). */
  @Test
  void testEnviar_IllegalArgumentException() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";

    when(emailService.generateAndSendCode(email, tipo))
        .thenThrow(new IllegalArgumentException("Limite atingido"));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, tipo));

    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Limite atingido"));
  }

  /** Teste de exceção IOException (ex: erro na API SendGrid). */
  @Test
  void testEnviar_IOException() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";

    when(emailService.generateAndSendCode(email, tipo))
        .thenThrow(new IOException("Erro na API SendGrid"));

    IOException ex = assertThrows(IOException.class, () -> controller.enviar(email, tipo));

    ResponseEntity<?> response = controller.handleIOException(ex);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Falha ao enviar e-mail"));
  }

  /** Teste para e-mail inválido (regex falha). */
  @Test
  void testEnviar_EmailInvalido() throws IOException {
    String email = "email-invalido";
    String tipo = "cadastro";

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, tipo));

    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Email inválido"));
  }

  /** Teste para tipo de código vazio. */
  @Test
  void testEnviar_TipoVazio() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "";

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, tipo));

    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Tipo de código não informado"));
  }

  /** Teste para e-mail nulo. */
  @Test
  void testEnviar_EmailNulo() throws IOException {
    String tipo = "cadastro";

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.enviar(null, tipo));

    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Email inválido"));
  }

  /** Teste para tipo nulo. */
  @Test
  void testEnviar_TipoNulo() throws IOException {
    String email = "teste@utfpr.edu.br";

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, null));

    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Tipo de código não informado"));
  }
}
