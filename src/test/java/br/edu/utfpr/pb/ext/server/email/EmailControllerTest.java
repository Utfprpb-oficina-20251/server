package br.edu.utfpr.pb.ext.server.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
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
    String tipo = "OTP_CADASTRO";

    when(emailService.generateAndSendCode(email, TipoCodigo.OTP_CADASTRO)).thenReturn(null);

    ResponseEntity<?> response = controller.enviar(email, tipo);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Código enviado com sucesso"));
  }

  /** Teste de validação de código verdadeiro. */
  @Test
  void testValidar_Success() {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    String codigo = "ABC123";

    when(validationService.validateCode(email, TipoCodigo.OTP_CADASTRO, codigo)).thenReturn(true);

    ResponseEntity<Boolean> response = controller.validar(email, tipo, codigo);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody());
  }

  /** Teste de validação de código falso. */
  @Test
  void testValidar_CodigoInvalido() {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    String codigo = "XYZ999";

    when(validationService.validateCode(email, TipoCodigo.OTP_CADASTRO, codigo)).thenReturn(false);

    ResponseEntity<Boolean> response = controller.validar(email, tipo, codigo);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody());
  }

  /** Teste de exceção IllegalArgumentException (ex: limite atingido). */
  @Test
  void testEnviar_IllegalArgumentException() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";

    when(emailService.generateAndSendCode(eq(email), any(TipoCodigo.class)))
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
    String tipo = "OTP_CADASTRO";

    when(emailService.generateAndSendCode(eq(email), any(TipoCodigo.class)))
        .thenThrow(new IOException("Erro na API SendGrid"));

    IOException ex = assertThrows(IOException.class, () -> controller.enviar(email, tipo));

    ResponseEntity<?> response = controller.handleIOException(ex);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Falha ao enviar e-mail"));
  }

  /** Teste para e-mail inválido (regex falha). */
  @Test
  void testEnviar_EmailInvalido() {
    String email = "email-invalido";
    String tipo = "OTP_CADASTRO";

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, tipo));

    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Email inválido"));
  }

  /** Teste para tipo de código vazio. */
  @Test
  void testEnviar_TipoVazio() {
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
  void testEnviar_EmailNulo() {
    String tipo = "OTP_CADASTRO"; // Alterado para corresponder ao nome do enum

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.enviar(null, tipo));

    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Email inválido"));
  }

  /** Teste para tipo nulo. */
  @Test
  void testEnviar_TipoNulo() {
    String email = "teste@utfpr.edu.br";

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, null));

    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Tipo de código não informado"));
  }

  /** Teste para tipo inválido. */
  @Test
  void testEnviar_TipoInvalido() {
    String email = "teste@utfpr.edu.br";
    String tipo = "TIPO_INEXISTENTE";

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, tipo));

    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Tipo de código inválido"));
  }
/** Teste para validação com código nulo. */
  @Test
  void testValidar_CodigoNulo() {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.validar(email, tipo, null));
    
    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Tipo de código não informado", ((Map<?, ?>) response.getBody()).get("erro"));
  }
  
  /** Teste para validação com código vazio. */
  @Test
  void testValidar_CodigoVazio() {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    String codigo = "";
    
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.validar(email, tipo, codigo));
    
    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Tipo de código não informado", ((Map<?, ?>) response.getBody()).get("erro"));
  }
  
  /** Teste para validação com email nulo. */
  @Test
  void testValidar_EmailNulo() {
    String tipo = "OTP_CADASTRO";
    String codigo = "ABC123";
    
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.validar(null, tipo, codigo));
    
    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Email inválido", ((Map<?, ?>) response.getBody()).get("erro"));
  }
  
  /** Teste para validação com tipo nulo no método validar. */
  @Test
  void testValidar_TipoNuloValidar() {
    String email = "teste@utfpr.edu.br";
    String codigo = "ABC123";
    
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.validar(email, null, codigo));
    
    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Tipo de código não informado", ((Map<?, ?>) response.getBody()).get("erro"));
  }
  
  /** Teste de envio com todos os tipos de código válidos. */
  @Test
  void testEnviar_TodosTiposCodigo() throws IOException {
    String email = "teste@utfpr.edu.br";
    String[] tipos = {"OTP_CADASTRO", "OTP_AUTENTICACAO", "OTP_RECUPERACAO"};
    
    for (String tipo : tipos) {
      when(emailService.generateAndSendCode(eq(email), any(TipoCodigo.class))).thenReturn(null);
      
      ResponseEntity<?> response = controller.enviar(email, tipo);
      
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertTrue(response.getBody().toString().contains("Código enviado com sucesso"));
      
      reset(emailService); // Reset mock for next iteration
    }
  }
  
  /** Teste para validação com todos os tipos de código válidos. */
  @Test
  void testValidar_TodosTiposCodigo() {
    String email = "teste@utfpr.edu.br";
    String codigo = "ABC123";
    String[] tipos = {"OTP_CADASTRO", "OTP_AUTENTICACAO", "OTP_RECUPERACAO"};
    TipoCodigo[] tiposEnum = {TipoCodigo.OTP_CADASTRO, TipoCodigo.OTP_AUTENTICACAO, TipoCodigo.OTP_RECUPERACAO};
    
    for (int i = 0; i < tipos.length; i++) {
      when(validationService.validateCode(email, tiposEnum[i], codigo)).thenReturn(true);
      
      ResponseEntity<Boolean> response = controller.validar(email, tipos[i], codigo);
      
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertTrue(response.getBody());
      
      reset(validationService);
    }
  }
  
  /** Teste para diferentes formatos de email inválidos. */
  @Test
  void testEnviar_DiferentesEmailsInvalidos() {
    String[] emailsInvalidos = {
      "email-sem-arroba",
      "@dominio.com", 
      "email@",
      "email@dominio",
      "email..duplo@dominio.com",
      "email@dominio..com",
      "email com espaço@dominio.com",
      "",
      "   ",
      "email@.com",
      "email@dominio."
    };
    String tipo = "OTP_CADASTRO";
    
    for (String email : emailsInvalidos) {
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, tipo));
      assertEquals("Email inválido", ex.getMessage());
    }
  }
  
  /** Teste para emails válidos de diferentes domínios. */
  @Test
  void testEnviar_DiferentesEmailsValidos() throws IOException {
    String[] emailsValidos = {
      "usuario@utfpr.edu.br",
      "test.email@gmail.com",
      "user-name@yahoo.com.br",
      "admin@empresa.com.br",
      "contato@site.org",
      "test123@domain.co.uk",
      "a@b.co"
    };
    String tipo = "OTP_CADASTRO";
    
    for (String email : emailsValidos) {
      when(emailService.generateAndSendCode(eq(email), any(TipoCodigo.class))).thenReturn(null);
      
      ResponseEntity<?> response = controller.enviar(email, tipo);
      
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertTrue(response.getBody().toString().contains("Código enviado com sucesso"));
      
      reset(emailService);
    }
  }
  
  /** Teste para código com diferentes formatos válidos. */
  @Test
  void testValidar_DiferentesFormatosCodigo() {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    String[] codigos = {"123456", "ABC123", "abcdef", "A1B2C3", "000000", "ZZZZZZ", "1234"};
    
    for (String codigo : codigos) {
      when(validationService.validateCode(email, TipoCodigo.OTP_CADASTRO, codigo)).thenReturn(true);
      
      ResponseEntity<Boolean> response = controller.validar(email, tipo, codigo);
      
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertTrue(response.getBody());
      
      reset(validationService);
    }
  }
  
  /** Teste para códigos rejeitados pelo serviço. */
  @Test
  void testValidar_CodigosRejeitados() {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    String[] codigosRejeitados = {"WRONG1", "XYZ999", "000001", "ABCDE1", "123457"};
    
    for (String codigo : codigosRejeitados) {
      when(validationService.validateCode(email, TipoCodigo.OTP_CADASTRO, codigo)).thenReturn(false);
      
      ResponseEntity<Boolean> response = controller.validar(email, tipo, codigo);
      
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertFalse(response.getBody());
      
      reset(validationService);
    }
  }
  
  /** Teste para verificar se os mocks são chamados corretamente no envio. */
  @Test
  void testEnviar_VerificarChamadaMock() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    
    when(emailService.generateAndSendCode(email, TipoCodigo.OTP_CADASTRO)).thenReturn(null);
    
    controller.enviar(email, tipo);
    
    verify(emailService, times(1)).generateAndSendCode(email, TipoCodigo.OTP_CADASTRO);
    verifyNoMoreInteractions(emailService);
  }
  
  /** Teste para verificar se o mock de validação é chamado corretamente. */
  @Test
  void testValidar_VerificarChamadaMock() {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    String codigo = "ABC123";
    
    when(validationService.validateCode(email, TipoCodigo.OTP_CADASTRO, codigo)).thenReturn(true);
    
    controller.validar(email, tipo, codigo);
    
    verify(validationService, times(1)).validateCode(email, TipoCodigo.OTP_CADASTRO, codigo);
    verifyNoMoreInteractions(validationService);
  }
  
  /** Teste para verificar que não há interações desnecessárias nos mocks após exceção. */
  @Test
  void testEnviar_MockInteractionAposExcecao() throws IOException {
    String email = "email-invalido";
    String tipo = "OTP_CADASTRO";
    
    assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, tipo));
    
    // Verifica que o service não foi chamado devido à validação falhar antes
    verifyNoInteractions(emailService);
  }
  
  /** Teste para verificar case sensitivity do tipo de código. */
  @Test
  void testEnviar_TipoCaseSensitive() {
    String email = "teste@utfpr.edu.br";
    String[] tiposInvalidos = {"otp_cadastro", "Otp_Cadastro", "OTP_cadastro", "oTP_CADASTRO"};
    
    for (String tipo : tiposInvalidos) {
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, tipo));
      assertTrue(ex.getMessage().contains("Tipo de código inválido"));
    }
  }
  
  /** Teste para tipos de código completamente inválidos. */
  @Test
  void testEnviar_TiposCompletamenteInvalidos() {
    String email = "teste@utfpr.edu.br";
    String[] tiposInvalidos = {"INVALID_TYPE", "123", "OTP_INEXISTENTE", "RANDOM_STRING", "NULL", "OTP_"};
    
    for (String tipo : tiposInvalidos) {
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, tipo));
      assertTrue(ex.getMessage().contains("Tipo de código inválido"));
    }
  }
  
  /** Teste para múltiplas chamadas consecutivas do mesmo email. */
  @Test
  void testEnviar_MultiplasChmadasConsecutivas() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    
    // Primeira chamada bem-sucedida
    when(emailService.generateAndSendCode(email, TipoCodigo.OTP_CADASTRO)).thenReturn(null);
    ResponseEntity<?> response1 = controller.enviar(email, tipo);
    assertEquals(HttpStatus.OK, response1.getStatusCode());
    
    // Segunda chamada com limite atingido
    when(emailService.generateAndSendCode(email, TipoCodigo.OTP_CADASTRO))
        .thenThrow(new IllegalArgumentException("Limite de tentativas atingido"));
    
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, tipo));
    assertEquals("Limite de tentativas atingido", ex.getMessage());
  }
  
  /** Teste para exceção RuntimeException não tratada. */
  @Test
  void testEnviar_RuntimeException() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    
    when(emailService.generateAndSendCode(eq(email), any(TipoCodigo.class)))
        .thenThrow(new RuntimeException("Erro inesperado"));
    
    assertThrows(RuntimeException.class, () -> controller.enviar(email, tipo));
  }
  
  /** Teste para email com caracteres especiais válidos. */
  @Test
  void testEnviar_EmailComCaracteresEspeciais() throws IOException {
    String email = "test.email-tag@sub.domain.utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    
    when(emailService.generateAndSendCode(email, TipoCodigo.OTP_CADASTRO)).thenReturn(null);
    
    ResponseEntity<?> response = controller.enviar(email, tipo);
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Código enviado com sucesso"));
  }
  
  /** Teste com email no limite de caracteres válidos. */
  @Test
  void testEnviar_EmailLimiteCaracteres() throws IOException {
    // Cria um email com muitos caracteres mas ainda válido
    String longEmail = "very.long.email.address.for.testing.purposes@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    
    when(emailService.generateAndSendCode(longEmail, TipoCodigo.OTP_CADASTRO)).thenReturn(null);
    
    ResponseEntity<?> response = controller.enviar(longEmail, tipo);
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().toString().contains("Código enviado com sucesso"));
  }
  
  /** Teste para interação completa entre envio e validação. */
  @Test
  void testFluxoCompletoEnvioEValidacao() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    String codigo = "ABC123";
    
    // Primeiro envia o código
    when(emailService.generateAndSendCode(email, TipoCodigo.OTP_CADASTRO)).thenReturn(null);
    ResponseEntity<?> envioResponse = controller.enviar(email, tipo);
    assertEquals(HttpStatus.OK, envioResponse.getStatusCode());
    
    // Depois valida o código
    when(validationService.validateCode(email, TipoCodigo.OTP_CADASTRO, codigo)).thenReturn(true);
    ResponseEntity<Boolean> validacaoResponse = controller.validar(email, tipo, codigo);
    assertEquals(HttpStatus.OK, validacaoResponse.getStatusCode());
    assertTrue(validacaoResponse.getBody());
    
    // Verifica que ambos os services foram chamados
    verify(emailService, times(1)).generateAndSendCode(email, TipoCodigo.OTP_CADASTRO);
    verify(validationService, times(1)).validateCode(email, TipoCodigo.OTP_CADASTRO, codigo);
  }
  
  /** Teste para verificar estrutura da resposta de sucesso no envio. */
  @Test
  void testEnviar_EstruturaRespostaSucesso() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    
    when(emailService.generateAndSendCode(email, TipoCodigo.OTP_CADASTRO)).thenReturn(null);
    
    ResponseEntity<?> response = controller.enviar(email, tipo);
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    
    @SuppressWarnings("unchecked")
    Map<String, String> responseBody = (Map<String, String>) response.getBody();
    assertEquals("Código enviado com sucesso", responseBody.get("mensagem"));
    assertEquals(email, responseBody.get("email"));
    assertEquals("OTP_CADASTRO", responseBody.get("tipo"));
  }
  
  /** Teste para verificar estrutura da resposta de erro. */
  @Test
  void testEnviar_EstruturaRespostaErro() {
    String email = "email-invalido";
    String tipo = "OTP_CADASTRO";
    
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> controller.enviar(email, tipo));
    ResponseEntity<?> response = controller.handleIllegalArgumentException(ex);
    
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    
    @SuppressWarnings("unchecked")
    Map<String, String> responseBody = (Map<String, String>) response.getBody();
    assertEquals("Email inválido", responseBody.get("erro"));
  }
  
  /** Teste para diferentes tipos de IOException. */
  @Test
  void testEnviar_DiferentesTiposIOException() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "OTP_CADASTRO";
    String[] mensagensErro = {
      "Erro na API SendGrid",
      "Timeout de conexão",
      "Falha na rede",
      "Serviço indisponível"
    };
    
    for (String mensagem : mensagensErro) {
      when(emailService.generateAndSendCode(eq(email), any(TipoCodigo.class)))
          .thenThrow(new IOException(mensagem));
      
      IOException ex = assertThrows(IOException.class, () -> controller.enviar(email, tipo));
      ResponseEntity<?> response = controller.handleIOException(ex);
      
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
      assertTrue(response.getBody().toString().contains("Falha ao enviar e-mail"));
      assertTrue(response.getBody().toString().contains(mensagem));
      
      reset(emailService);
    }
  }
}
