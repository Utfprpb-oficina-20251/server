package br.edu.utfpr.pb.ext.server.test;

import br.edu.utfpr.pb.ext.server.model.EmailCode;
import br.edu.utfpr.pb.ext.server.repository.EmailCodeRepository;
import br.edu.utfpr.pb.ext.server.service.impl.EmailServiceImpl;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.List;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para EmailServiceImpl, garantindo que a geração e envio de código
 * funcione corretamente em diferentes cenários.
 */
public class EmailServiceImplTest {

    private EmailServiceImpl emailService;
    private EmailCodeRepository emailCodeRepository;
    private SendGrid sendGrid;

    @BeforeEach
    void setUp() {
        emailCodeRepository = mock(EmailCodeRepository.class);
        sendGrid = mock(SendGrid.class);

        emailService = new EmailServiceImpl();
        // Injeção manual dos mocks (já que não usamos @Autowired no teste)
        emailService.getClass().getDeclaredFields();

        // Usa reflexão para injetar dependências privadas
        injectPrivateField(emailService, "emailCodeRepository", emailCodeRepository);
        injectPrivateField(emailService, "sendGrid", sendGrid);
    }

    /**
     * Teste para verificar envio com sucesso.
     */
    @Test
    void testGenerateAndSendCode_Success() throws IOException {
        String email = "teste@utfpr.edu.br";
        String tipo = "cadastro";

        // Nenhum código anterior nos últimos 24h
        when(emailCodeRepository.findTopByEmailAndTypeOrderByGeneratedAtDesc(email, tipo))
                .thenReturn(Optional.empty());

        // Mock da resposta positiva da API SendGrid
        when(sendGrid.api(any())).thenReturn(new Response(202, "", null));

        Response response = emailService.generateAndSendCode(email, tipo);

        assertEquals(202, response.getStatusCode());

        // Verifica se salvou o código no banco
        verify(emailCodeRepository).save(any(EmailCode.class));
    }

    /**
     * Teste para validar que o limite de envio diário é respeitado.
     */
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

        // Mocka o SendGrid para evitar NullPointerException
        when(sendGrid.api(any())).thenReturn(new Response(202, "", null));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            emailService.generateAndSendCode(email, tipo);
        });

        assertEquals("Limite de códigos enviados para este e-mail nas últimas 24h.", ex.getMessage());
    }

    /**
     * Teste para simular falha no envio pelo SendGrid.
     */
    @Test
    void testGenerateAndSendCode_SendGridFails() throws IOException {
        String email = "teste@utfpr.edu.br";
        String tipo = "cadastro";

        when(emailCodeRepository.findTopByEmailAndTypeOrderByGeneratedAtDesc(email, tipo))
                .thenReturn(Optional.empty());

        // Mock da resposta com erro
        when(sendGrid.api(any())).thenReturn(new Response(400, "", null));

        IOException ex = assertThrows(IOException.class, () -> {
            emailService.generateAndSendCode(email, tipo);
        });

        assertTrue(ex.getMessage().contains("Erro ao enviar e-mail via SendGrid"));
    }

    // Utilitário para injetar mocks em campos privados
    private void injectPrivateField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao injetar dependência via reflexão", e);
        }
    }
}