package br.edu.utfpr.pb.ext.server.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.email.impl.EmailServiceImpl;
import br.edu.utfpr.pb.ext.server.email.enums.TipoDeNotificacao;
import com.sendgrid.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

/** Testes para a função enviarEmailDeNotificacao da classe EmailServiceImpl. */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private SendGrid sendGrid;

    @InjectMocks
    private EmailServiceImpl emailService;

    private final String email = "teste@utfpr.edu.br";
    private final String projeto = "Projeto X";
    private final String link = "https://utfpr.edu.br/link";

    @BeforeEach
    void setUp() {}

    @Test
    void testInscricaoAluno() throws IOException {
        Response fakeResponse = new Response(202, "Accepted", null);
        when(sendGrid.api(any())).thenReturn(fakeResponse);

        Response response = emailService.enviarEmailDeNotificacao(email, TipoDeNotificacao.INSCRICAO_ALUNO, projeto, link);

        assertEquals(202, response.getStatusCode());
        verify(sendGrid, times(1)).api(any());
    }

    @Test
    void testInscricaoAlunoProfessor() throws IOException {
        Response fakeResponse = new Response(202, "Accepted", null);
        when(sendGrid.api(any())).thenReturn(fakeResponse);

        Response response = emailService.enviarEmailDeNotificacao(email, TipoDeNotificacao.INSCRICAO_ALUNO_PROFESSOR, projeto, link);

        assertEquals(202, response.getStatusCode());
        verify(sendGrid).api(any());
    }

    @Test
    void testAtualizacaoStatus() throws IOException {
        Response fakeResponse = new Response(202, "Accepted", null);
        when(sendGrid.api(any())).thenReturn(fakeResponse);

        Response response = emailService.enviarEmailDeNotificacao(email, TipoDeNotificacao.ATUALIZACAO_STATUS, projeto, link);

        assertEquals(202, response.getStatusCode());
        verify(sendGrid).api(any());
    }

    @Test
    void testSendGridFalha() throws IOException {
        when(sendGrid.api(any())).thenThrow(new IOException("Erro no SendGrid"));

        IOException ex = assertThrows(IOException.class, () ->
                emailService.enviarEmailDeNotificacao(email, TipoDeNotificacao.INSCRICAO_ALUNO, projeto, link)
        );

        assertEquals("Erro no SendGrid", ex.getMessage());
    }
    @Test
    void testEnviarEmailDeNotificacao_TipoNulo_DeveLancarExcecao() {
        String email = "teste@utfpr.edu.br";
        String projeto = "Projeto Qualquer";
        String link = "https://utfpr.edu.br/link";

        var ex = assertThrows(IllegalArgumentException.class, () -> {
            Response response = emailService.enviarEmailDeNotificacao(email, null, projeto, link);
        });

        assertEquals("Tipo de notificação não pode ser nulo.", ex.getMessage());
    }
    @Test
    void testEnviarEmailDeNotificacao_EmailNulo_DeveLancarExcecao() {
        TipoDeNotificacao tipo = TipoDeNotificacao.INSCRICAO_ALUNO;
        String projeto = "Projeto Qualquer";
        String link = "https://utfpr.edu.br/link";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> emailService.enviarEmailDeNotificacao(null, tipo, projeto, link));

        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }
    @Test
    void testEnviarEmailDeNotificacao_ProjetoNulo_DeveLancarExcecao() {
        TipoDeNotificacao tipo = TipoDeNotificacao.INSCRICAO_ALUNO;
        String email = "teste@utfpr.edu.br";
        String link = "https://utfpr.edu.br/link";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> emailService.enviarEmailDeNotificacao(email, tipo, null, link));

        assertTrue(ex.getMessage().toLowerCase().contains("projeto"));
    }
}
