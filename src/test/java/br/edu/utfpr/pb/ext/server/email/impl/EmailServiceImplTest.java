package br.edu.utfpr.pb.ext.server.email.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.email.EmailCode;
import br.edu.utfpr.pb.ext.server.email.EmailCodeRepository;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import java.io.IOException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
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

  public static final long MAX_DAILY_LIMIT_MAIS_UM = 31L;
  public static final String ERRO_LIMITE_DIARIO_ATINGIDO =
      "Quantidade de solicitações ultrapassa o limite das últimas 24 horas.";
  private static final Long MAX_SHORT_PERIOD_MAIS_UM = 6L;
  private static final String ERRO_LIMITE_CURTO =
      "Limite de solicitações atingido, tente novamente em %d minutos.".formatted(15L);
  @Mock private EmailCodeRepository emailCodeRepository;
  @Mock private SendGrid sendGrid;
  @InjectMocks private EmailServiceImpl emailService;

  /** Teste para verificar envio com sucesso. */
  @Test
  void testGenerateAndSendCode_Success() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";

    when(emailCodeRepository.countByEmailAndTypeAndGeneratedAtAfter(any(), any(), any()))
        .thenReturn(0L);
    when(sendGrid.api(any())).thenReturn(new Response(202, "", null));

    Response response = emailService.generateAndSendCode(email, tipo);

    assertEquals(202, response.getStatusCode());

    ArgumentCaptor<EmailCode> codeCaptor = ArgumentCaptor.forClass(EmailCode.class);
    verify(emailCodeRepository).save(codeCaptor.capture());

    EmailCode savedCode = codeCaptor.getValue();
    assertEquals(email, savedCode.getEmail());
    assertEquals(tipo, savedCode.getType());
    assertNotNull(savedCode.getCode());
    assertFalse(savedCode.isUsed());
    assertNotNull(savedCode.getGeneratedAt());
    assertNotNull(savedCode.getExpiration());
    assertFalse(savedCode.getCode().isEmpty());
    assertTrue(savedCode.getExpiration().isAfter(savedCode.getGeneratedAt()));
    assertTrue(savedCode.getExpiration().isAfter(LocalDateTime.now()));
  }

  /** Teste para validar que o limite de envio diário é respeitado. */
  @Test
  void testGenerateAndSendCode_MaxDailyLimitReached() {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";

    when(emailCodeRepository.countByEmailAndTypeAndGeneratedAtAfter(any(), any(), any()))
        .thenReturn(MAX_DAILY_LIMIT_MAIS_UM);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> emailService.generateAndSendCode(email, tipo));

    assertEquals(ERRO_LIMITE_DIARIO_ATINGIDO, ex.getMessage());
  }

  @Test
  @DisplayName("Valida que o serviço gera erro ao ultrapassar o limite curto de envios")
  void generateAndSendCode_WhenLimiteCurtoFoiUltrapassado_ReturnErroLimiteCurto() {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";

    when(emailCodeRepository.countByEmailAndTypeAndGeneratedAtAfter(any(), any(), any()))
        .thenReturn(MAX_SHORT_PERIOD_MAIS_UM);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> emailService.generateAndSendCode(email, tipo));
    assertEquals(ERRO_LIMITE_CURTO, ex.getMessage());
  }

  /** Teste para simular falha no envio pelo SendGrid. */
  @Test
  void testGenerateAndSendCode_SendGridFails() throws IOException {
    String email = "teste@utfpr.edu.br";
    String tipo = "cadastro";
    Response errorResponse = new Response(400, "", null);

    when(emailCodeRepository.countByEmailAndTypeAndGeneratedAtAfter(any(), any(), any()))
        .thenReturn(0L);
    when(sendGrid.api(any())).thenReturn(errorResponse);

    IOException ex =
        assertThrows(IOException.class, () -> emailService.generateAndSendCode(email, tipo));

    assertTrue(ex.getMessage().contains("Erro ao enviar e-mail"));
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

  @Test
  @DisplayName("Garante que erro é retornado no caso de não haver o tipo do código na solicitação")
  void testGenerateAndSendSendCode_whenTypeIsNull_ShouldReturnIllegalArgumentException() {
    String email = "teste@utfpr.edu.br";
    String type = null;

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> emailService.generateAndSendCode(email, type));
    assertEquals("O tipo do código é obrigatório.", ex.getMessage());
  }
}
