package br.edu.utfpr.pb.ext.server.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.EmailOtpAuthRequestDTO;
import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationProvider;
import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationToken;
import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import br.edu.utfpr.pb.ext.server.email.impl.EmailServiceImpl;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.authority.Authority;
import br.edu.utfpr.pb.ext.server.usuario.authority.AuthorityRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private EmailServiceImpl emailService;

    @Mock
    private EmailOtpAuthenticationProvider emailOtpAuthenticationProvider;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthService authService;

    private CadastroUsuarioDTO cadastroAlunoDTO;
    private CadastroUsuarioDTO cadastroServidorDTO;
    private CadastroUsuarioDTO cadastroInvalidoDTO;
    private Usuario usuarioAluno;
    private Usuario usuarioServidor;
    private Authority authorityAluno;
    private Authority authorityServidor;
    private EmailOtpAuthRequestDTO emailOtpAuthRequestDTO;

    @BeforeEach
    void setUp() {
        cadastroAlunoDTO = CadastroUsuarioDTO.builder()
            .nome("João Silva")
            .email("joao@alunos.utfpr.edu.br")
            .registro("12345678901")
            .build();

        cadastroServidorDTO = CadastroUsuarioDTO.builder()
            .nome("Maria Santos")
            .email("maria@utfpr.edu.br")
            .registro("98765432100")
            .build();

        cadastroInvalidoDTO = CadastroUsuarioDTO.builder()
            .nome("Pedro Invalid")
            .email("pedro@gmail.com")
            .registro("11111111111")
            .build();

        authorityAluno = Authority.builder()
            .authority("ROLE_ALUNO")
            .build();

        authorityServidor = Authority.builder()
            .authority("ROLE_SERVIDOR")
            .build();

        Set<Authority> authoritiesAluno = new HashSet<>();
        authoritiesAluno.add(authorityAluno);

        Set<Authority> authoritiesServidor = new HashSet<>();
        authoritiesServidor.add(authorityServidor);

        usuarioAluno = Usuario.builder()
            .id(1L)
            .nome("João Silva")
            .email("joao@alunos.utfpr.edu.br")
            .cpf("12345678901")
            .authorities(authoritiesAluno)
            .build();

        usuarioServidor = Usuario.builder()
            .id(2L)
            .nome("Maria Santos")
            .email("maria@utfpr.edu.br")
            .cpf("98765432100")
            .authorities(authoritiesServidor)
            .build();

        emailOtpAuthRequestDTO = EmailOtpAuthRequestDTO.builder()
            .email("joao@alunos.utfpr.edu.br")
            .code("123456")
            .build();
    }

    @Nested
    @DisplayName("Cadastro de Usuário Tests")
    class CadastroTests {

        @Test
        @DisplayName("Deve cadastrar aluno com sucesso")
        void deveCadastrarAlunoComSucesso() {
            // Given
            when(usuarioRepository.findByEmail(cadastroAlunoDTO.getEmail())).thenReturn(Optional.empty());
            when(authorityRepository.findByAuthority("ROLE_ALUNO")).thenReturn(Optional.of(authorityAluno));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioAluno);

            // When
            Usuario resultado = authService.cadastro(cadastroAlunoDTO);

            // Then
            assertNotNull(resultado);
            assertEquals("João Silva", resultado.getNome());
            assertEquals("joao@alunos.utfpr.edu.br", resultado.getEmail());
            assertEquals("12345678901", resultado.getCpf());
            assertTrue(resultado.getAuthorities().contains(authorityAluno));

            verify(usuarioRepository).findByEmail(cadastroAlunoDTO.getEmail());
            verify(authorityRepository).findByAuthority("ROLE_ALUNO");
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve cadastrar servidor com sucesso")
        void deveCadastrarServidorComSucesso() {
            // Given
            when(usuarioRepository.findByEmail(cadastroServidorDTO.getEmail())).thenReturn(Optional.empty());
            when(authorityRepository.findByAuthority("ROLE_SERVIDOR")).thenReturn(Optional.of(authorityServidor));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioServidor);

            // When
            Usuario resultado = authService.cadastro(cadastroServidorDTO);

            // Then
            assertNotNull(resultado);
            assertEquals("Maria Santos", resultado.getNome());
            assertEquals("maria@utfpr.edu.br", resultado.getEmail());
            assertEquals("98765432100", resultado.getCpf());
            assertTrue(resultado.getAuthorities().contains(authorityServidor));

            verify(usuarioRepository).findByEmail(cadastroServidorDTO.getEmail());
            verify(authorityRepository).findByAuthority("ROLE_SERVIDOR");
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário já existe")
        void deveLancarExcecaoQuandoUsuarioJaExiste() {
            // Given
            when(usuarioRepository.findByEmail(cadastroAlunoDTO.getEmail())).thenReturn(Optional.of(usuarioAluno));

            // When & Then
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                authService.cadastro(cadastroAlunoDTO);
            });

            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
            assertEquals("Usuário já cadastrado", exception.getReason());

            verify(usuarioRepository).findByEmail(cadastroAlunoDTO.getEmail());
            verifyNoInteractions(authorityRepository);
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando authority não existe")
        void deveLancarExcecaoQuandoAuthorityNaoExiste() {
            // Given
            when(usuarioRepository.findByEmail(cadastroAlunoDTO.getEmail())).thenReturn(Optional.empty());
            when(authorityRepository.findByAuthority("ROLE_ALUNO")).thenReturn(Optional.empty());

            // When & Then
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                authService.cadastro(cadastroAlunoDTO);
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Erro ao cadastrar", exception.getReason());

            verify(usuarioRepository).findByEmail(cadastroAlunoDTO.getEmail());
            verify(authorityRepository).findByAuthority("ROLE_ALUNO");
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lançar exceção para email com domínio inválido")
        void deveLancarExcecaoParaEmailComDominioInvalido() {
            // When & Then
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                authService.cadastro(cadastroInvalidoDTO);
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("E-mail deve ser @utfpr.edu.br ou @alunos.utfpr.edu.br", exception.getReason());

            verifyNoInteractions(usuarioRepository);
            verifyNoInteractions(authorityRepository);
        }
    }

    @Nested
    @DisplayName("Solicitação de Código OTP Tests")
    class SolicitarCodigoOtpTests {

        @Test
        @DisplayName("Deve solicitar código OTP com sucesso")
        void deveSolicitarCodigoOtpComSucesso() {
            // Given
            String email = "joao@alunos.utfpr.edu.br";
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioAluno));
            doNothing().when(emailService).generateAndSendCode(email, TipoCodigo.OTP_AUTENTICACAO);

            // When & Then
            assertDoesNotThrow(() -> authService.solicitarCodigoOtp(email));

            verify(usuarioRepository).findByEmail(email);
            verify(emailService).generateAndSendCode(email, TipoCodigo.OTP_AUTENTICACAO);
        }

        @Test
        @DisplayName("Deve lançar exceção quando email não está cadastrado")
        void deveLancarExcecaoQuandoEmailNaoEstaCadastrado() {
            // Given
            String email = "naoexiste@alunos.utfpr.edu.br";
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

            // When & Then
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                authService.solicitarCodigoOtp(email);
            });

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Email não cadastrado", exception.getReason());

            verify(usuarioRepository).findByEmail(email);
            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("Deve lançar exceção quando falha no envio do email")
        void deveLancarExcecaoQuandoFalhaNoEnvioDoEmail() {
            // Given
            String email = "joao@alunos.utfpr.edu.br";
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioAluno));
            doThrow(new RuntimeException("Erro no envio")).when(emailService)
                .generateAndSendCode(email, TipoCodigo.OTP_AUTENTICACAO);

            // When & Then
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                authService.solicitarCodigoOtp(email);
            });

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
            assertEquals("Erro ao enviar código de verificação", exception.getReason());

            verify(usuarioRepository).findByEmail(email);
            verify(emailService).generateAndSendCode(email, TipoCodigo.OTP_AUTENTICACAO);
        }
    }

    @Nested
    @DisplayName("Autenticação OTP Tests")
    class AutenticacaoOtpTests {

        @Test
        @DisplayName("Deve autenticar com OTP válido")
        void deveAutenticarComOtpValido() {
            // Given
            EmailOtpAuthenticationToken authToken = new EmailOtpAuthenticationToken(
                emailOtpAuthRequestDTO.getEmail(),
                emailOtpAuthRequestDTO.getCode()
            );
            Authentication authentication = mock(Authentication.class);

            when(emailOtpAuthenticationProvider.authenticate(any(EmailOtpAuthenticationToken.class)))
                .thenReturn(authentication);
            when(usuarioRepository.findByEmail(emailOtpAuthRequestDTO.getEmail()))
                .thenReturn(Optional.of(usuarioAluno));

            SecurityContextHolder.setContext(securityContext);

            // When
            Usuario resultado = authService.autenticacaoOtp(emailOtpAuthRequestDTO);

            // Then
            assertNotNull(resultado);
            assertEquals(usuarioAluno.getId(), resultado.getId());
            assertEquals(usuarioAluno.getEmail(), resultado.getEmail());

            verify(emailOtpAuthenticationProvider).authenticate(any(EmailOtpAuthenticationToken.class));
            verify(securityContext).setAuthentication(authentication);
            verify(usuarioRepository).findByEmail(emailOtpAuthRequestDTO.getEmail());
        }

        @Test
        @DisplayName("Deve lançar exceção quando código OTP é inválido")
        void deveLancarExcecaoQuandoCodigoOtpEInvalido() {
            // Given
            when(emailOtpAuthenticationProvider.authenticate(any(EmailOtpAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Código inválido"));

            // When & Then
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                authService.autenticacaoOtp(emailOtpAuthRequestDTO);
            });

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
            assertEquals("Código inválido ou expirado", exception.getReason());

            verify(emailOtpAuthenticationProvider).authenticate(any(EmailOtpAuthenticationToken.class));
            verifyNoInteractions(usuarioRepository);
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não é encontrado após autenticação")
        void deveLancarExcecaoQuandoUsuarioNaoEEncontradoAposAutenticacao() {
            // Given
            EmailOtpAuthenticationToken authToken = new EmailOtpAuthenticationToken(
                emailOtpAuthRequestDTO.getEmail(),
                emailOtpAuthRequestDTO.getCode()
            );
            Authentication authentication = mock(Authentication.class);

            when(emailOtpAuthenticationProvider.authenticate(any(EmailOtpAuthenticationToken.class)))
                .thenReturn(authentication);
            when(usuarioRepository.findByEmail(emailOtpAuthRequestDTO.getEmail()))
                .thenReturn(Optional.empty());

            SecurityContextHolder.setContext(securityContext);

            // When & Then
            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
                authService.autenticacaoOtp(emailOtpAuthRequestDTO);
            });

            assertEquals("Email não cadastrado", exception.getMessage());

            verify(emailOtpAuthenticationProvider).authenticate(any(EmailOtpAuthenticationToken.class));
            verify(securityContext).setAuthentication(authentication);
            verify(usuarioRepository).findByEmail(emailOtpAuthRequestDTO.getEmail());
        }
    }

    @Nested
    @DisplayName("Validação de Domínio de Email Tests")
    class ValidacaoDominioEmailTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "aluno@alunos.utfpr.edu.br",
            "teste@alunos.utfpr.edu.br",
            "joao.silva@alunos.utfpr.edu.br",
            "maria_santos@alunos.utfpr.edu.br"
        })
        @DisplayName("Deve aceitar emails válidos de alunos")
        void deveAceitarEmailsValidosDeAlunos(String email) {
            // Given
            CadastroUsuarioDTO dto = CadastroUsuarioDTO.builder()
                .nome("Teste Aluno")
                .email(email)
                .registro("12345678901")
                .build();

            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(authorityRepository.findByAuthority("ROLE_ALUNO")).thenReturn(Optional.of(authorityAluno));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioAluno);

            // When & Then
            assertDoesNotThrow(() -> authService.cadastro(dto));

            verify(authorityRepository).findByAuthority("ROLE_ALUNO");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "servidor@utfpr.edu.br",
            "professor@utfpr.edu.br",
            "admin@utfpr.edu.br",
            "coordenador@utfpr.edu.br"
        })
        @DisplayName("Deve aceitar emails válidos de servidores")
        void deveAceitarEmailsValidosDeServidores(String email) {
            // Given
            CadastroUsuarioDTO dto = CadastroUsuarioDTO.builder()
                .nome("Teste Servidor")
                .email(email)
                .registro("98765432100")
                .build();

            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(authorityRepository.findByAuthority("ROLE_SERVIDOR")).thenReturn(Optional.of(authorityServidor));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioServidor);

            // When & Then
            assertDoesNotThrow(() -> authService.cadastro(dto));

            verify(authorityRepository).findByAuthority("ROLE_SERVIDOR");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "usuario@gmail.com",
            "test@yahoo.com",
            "invalid@hotmail.com",
            "user@empresa.com.br",
            "email@utfpr.com",
            "test@alunos.utfpr.com",
            "@utfpr.edu.br",
            "@alunos.utfpr.edu.br",
            "semdominio",
            ""
        })
        @DisplayName("Deve rejeitar emails com domínios inválidos")
        void deveRejeitarEmailsComDominiosInvalidos(String email) {
            // Given
            CadastroUsuarioDTO dto = CadastroUsuarioDTO.builder()
                .nome("Teste Invalid")
                .email(email)
                .registro("11111111111")
                .build();

            // When & Then
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                authService.cadastro(dto);
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("E-mail deve ser @utfpr.edu.br ou @alunos.utfpr.edu.br", exception.getReason());

            verifyNoInteractions(usuarioRepository);
            verifyNoInteractions(authorityRepository);
        }
    }

    @Nested
    @DisplayName("Edge Cases e Validações Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve lidar com DTO nulo no cadastro")
        void deveLidarComDtoNuloNoCadastro() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                authService.cadastro(null);
            });

            verifyNoInteractions(usuarioRepository);
            verifyNoInteractions(authorityRepository);
        }

        @Test
        @DisplayName("Deve lidar com email nulo na solicitação OTP")
        void deveLidarComEmailNuloNaSolicitacaoOtp() {
            // When & Then
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                authService.solicitarCodigoOtp(null);
            });

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

            verify(usuarioRepository).findByEmail(null);
            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("Deve lidar com email vazio na solicitação OTP")
        void deveLidarComEmailVazioNaSolicitacaoOtp() {
            // Given
            String emailVazio = "";
            when(usuarioRepository.findByEmail(emailVazio)).thenReturn(Optional.empty());

            // When & Then
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                authService.solicitarCodigoOtp(emailVazio);
            });

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Email não cadastrado", exception.getReason());

            verify(usuarioRepository).findByEmail(emailVazio);
            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("Deve lidar com DTO nulo na autenticação OTP")
        void deveLidarComDtoNuloNaAutenticacaoOtp() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                authService.autenticacaoOtp(null);
            });

            verifyNoInteractions(emailOtpAuthenticationProvider);
            verifyNoInteractions(usuarioRepository);
        }

        @Test
        @DisplayName("Deve lidar com authorities vazias")
        void deveLidarComAuthoritiesVazias() {
            // Given
            when(usuarioRepository.findByEmail(cadastroAlunoDTO.getEmail())).thenReturn(Optional.empty());
            when(authorityRepository.findByAuthority("ROLE_ALUNO")).thenReturn(Optional.of(authorityAluno));

            Usuario usuarioSemAuthorities = Usuario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@alunos.utfpr.edu.br")
                .cpf("12345678901")
                .authorities(new HashSet<>())
                .build();

            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSemAuthorities);

            // When
            Usuario resultado = authService.cadastro(cadastroAlunoDTO);

            // Then
            assertNotNull(resultado);
            assertNotNull(resultado.getAuthorities());
            // As authorities são definidas no método, então não devem estar vazias

            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve tratar caso sensível de domínio de email")
        void deveTratarCasoSensivelDeDominioDeEmail() {
            // Given
            CadastroUsuarioDTO dtoMixedCase = CadastroUsuarioDTO.builder()
                .nome("Teste Case")
                .email("teste@ALUNOS.UTFPR.EDU.BR")
                .registro("12345678901")
                .build();

            // When & Then - deve falhar porque o método é case-sensitive
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                authService.cadastro(dtoMixedCase);
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("E-mail deve ser @utfpr.edu.br ou @alunos.utfpr.edu.br", exception.getReason());
        }
    }
}