package br.edu.utfpr.pb.ext.server.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.model.EmailCode;
import br.edu.utfpr.pb.ext.server.repository.EmailCodeRepository;
import br.edu.utfpr.pb.ext.server.service.impl.EmailServiceImpl;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes unitários para EmailServiceImpl, garantindo que a geração e envio de código funcione
 * corretamente em diferentes cenários.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

  @Mock private EmailCodeRepository emailCodeRepository;
  @Mock private SendGrid sendGrid;
  @InjectMocks private EmailServiceImpl emailService;

  @BeforeEach
  void setUp() {}

  /** Teste para verificar envio com sucesso. */
  @Test
  void testGenerateAndSendCode_Success() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";

    // Simula que ainda não há códigos gerados
    when(emailCodeRepository.findAllByEmailAndTypeAndGeneratedAtAfter(any(), any(), any()))
        .thenReturn(Collections.emptyList());

    // Mock da resposta positiva da API SendGrid
    when(sendGrid.api(any())).thenReturn(new Response(202, "", null));

    Response response = emailService.generateAndSendCode(email, tipo);

    assertEquals(202, response.getStatusCode());

    // Captura e valida o objeto salvo no banco
    ArgumentCaptor<EmailCode> codeCaptor = ArgumentCaptor.forClass(EmailCode.class);
    verify(emailCodeRepository).save(codeCaptor.capture());

    EmailCode savedCode = codeCaptor.getValue();
    assertEquals(email, savedCode.getEmail());
    assertEquals(tipo, savedCode.getType());
    assertNotNull(savedCode.getCode());
    assertFalse(savedCode.isUsed());
    assertNotNull(savedCode.getGeneratedAt());
    assertNotNull(savedCode.getExpiration());

    // Validações adicionais
    assertTrue(savedCode.getCode().length() > 0);
    assertTrue(savedCode.getExpiration().isAfter(savedCode.getGeneratedAt()));
    assertTrue(savedCode.getExpiration().isAfter(LocalDateTime.now()));
  }

  /** Teste para validar que o limite de envio diário é respeitado. */
  @Test
  void testGenerateAndSendCode_MaxLimitReached() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";

    // Simula que já existem 3 códigos gerados nas últimas 24h
    EmailCode code1 = new EmailCode();
    code1.setGeneratedAt(LocalDateTime.now().minusMinutes(1));
    EmailCode code2 = new EmailCode();
    code2.setGeneratedAt(LocalDateTime.now().minusHours(1));
    EmailCode code3 = new EmailCode();
    code3.setGeneratedAt(LocalDateTime.now().minusHours(2));

    when(emailCodeRepository.findAllByEmailAndTypeAndGeneratedAtAfter(any(), any(), any()))
        .thenReturn(List.of(code1, code2, code3));

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> emailService.generateAndSendCode(email, tipo));

    assertEquals("Limite de códigos enviados para este e-mail nas últimas 24h.", ex.getMessage());
  }

  /** Teste para simular falha no envio pelo SendGrid. */
  @Test
  void testGenerateAndSendCode_SendGridFails() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";
    Response errorResponse = new Response(400, "", null);

    // Simula que ainda não há códigos gerados
    when(emailCodeRepository.findAllByEmailAndTypeAndGeneratedAtAfter(any(), any(), any()))
        .thenReturn(Collections.emptyList());

    // Mock da resposta com erro
    when(sendGrid.api(any())).thenReturn(errorResponse);

    IOException ex =
        assertThrows(IOException.class, () -> emailService.generateAndSendCode(email, tipo));

    assertTrue(ex.getMessage().contains("Erro ao enviar e-mail via SendGrid"));
  }

  /** Teste para e-mail inválido (regex não passa). */
  @Test
  void testGenerateAndSendCode_InvalidEmail() {
    String email = "email_invalido";
    String tipo = "cadastro";

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> emailService.generateAndSendCode(email, tipo));

    assertEquals("Endereço de e-mail inválido.", ex.getMessage());
  }

  /** Teste para tipo nulo (se a regra permitir). */
  @Test
  void testGenerateAndSendCode_NullTypeButValidEmail() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = null;

    when(emailCodeRepository.findAllByEmailAndTypeAndGeneratedAtAfter(any(), any(), any()))
        .thenReturn(Collections.emptyList());

    when(sendGrid.api(any())).thenReturn(new Response(202, "", null));

    // Não lança exceção pois o tipo nulo é aceito no service
    assertDoesNotThrow(() -> emailService.generateAndSendCode(email, tipo));
  }
}
