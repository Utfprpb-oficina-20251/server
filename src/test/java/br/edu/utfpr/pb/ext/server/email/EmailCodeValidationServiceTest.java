package br.edu.utfpr.pb.ext.server.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.email.EmailCodeValidation;
import br.edu.utfpr.pb.ext.server.email.EmailCodeValidationRepository;
import br.edu.utfpr.pb.ext.server.email.EmailCodeValidationService;
import br.edu.utfpr.pb.ext.server.email.EmailService;
import br.edu.utfpr.pb.ext.server.user.User;
import br.edu.utfpr.pb.ext.server.user.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailCodeValidationService Tests")
class EmailCodeValidationServiceTest {

    @Mock
    private EmailCodeValidationRepository emailCodeValidationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailCodeValidationService emailCodeValidationService;

    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_CODE = "123456";
    private EmailCodeValidation validCodeValidation;
    private User testUser;

    @BeforeEach
    void setUp() {
        validCodeValidation = EmailCodeValidation.builder()
                .email(VALID_EMAIL)
                .code(VALID_CODE)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        testUser = User.builder()
                .email(VALID_EMAIL)
                .emailVerified(false)
                .build();
    }

    @Test
    @DisplayName("Should generate and send code successfully for valid email")
    void should_GenerateAndSendCode_When_ValidEmailProvided() {
        // Given
        when(emailCodeValidationRepository.save(any(EmailCodeValidation.class)))
                .thenReturn(validCodeValidation);

        // When
        String result = emailCodeValidationService.generateAndSendCode(VALID_EMAIL);

        // Then
        assertNotNull(result);
        assertEquals(6, result.length());
        assertTrue(result.matches("\\d{6}"));

        verify(emailCodeValidationRepository).save(any(EmailCodeValidation.class));
        verify(emailService).sendValidationCode(eq(VALID_EMAIL), anyString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Should handle null and empty email addresses")
    void should_HandleNullAndEmptyEmails_When_GeneratingCode(String email) {
        // When & Then
        assertDoesNotThrow(() -> emailCodeValidationService.generateAndSendCode(email));
        verify(emailCodeValidationRepository).save(any(EmailCodeValidation.class));
        verify(emailService).sendValidationCode(eq(email), anyString());
    }

    @Test
    @DisplayName("Should generate different codes for multiple requests")
    void should_GenerateDifferentCodes_When_MultipleRequestsMade() {
        // Given
        when(emailCodeValidationRepository.save(any(EmailCodeValidation.class)))
                .thenReturn(validCodeValidation);

        // When
        String code1 = emailCodeValidationService.generateAndSendCode(VALID_EMAIL);
        String code2 = emailCodeValidationService.generateAndSendCode(VALID_EMAIL);

        // Then
        assertNotEquals(code1, code2);
        verify(emailCodeValidationRepository, times(2)).save(any(EmailCodeValidation.class));
        verify(emailService, times(2)).sendValidationCode(eq(VALID_EMAIL), anyString());
    }

    @Test
    @DisplayName("Should handle repository save failure gracefully")
    void should_HandleRepositoryFailure_When_SavingCode() {
        // Given
        when(emailCodeValidationRepository.save(any(EmailCodeValidation.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                emailCodeValidationService.generateAndSendCode(VALID_EMAIL));

        verify(emailCodeValidationRepository).save(any(EmailCodeValidation.class));
        verify(emailService, never()).sendValidationCode(anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle email service failure gracefully")
    void should_HandleEmailServiceFailure_When_SendingCode() {
        // Given
        when(emailCodeValidationRepository.save(any(EmailCodeValidation.class)))
                .thenReturn(validCodeValidation);
        doThrow(new RuntimeException("Email service error"))
                .when(emailService).sendValidationCode(anyString(), anyString());

        // When & Then
        assertThrows(RuntimeException.class, () ->
                emailCodeValidationService.generateAndSendCode(VALID_EMAIL));

        verify(emailCodeValidationRepository).save(any(EmailCodeValidation.class));
        verify(emailService).sendValidationCode(eq(VALID_EMAIL), anyString());
    }

    @Test
    @DisplayName("Should validate code successfully when code is valid and not expired")
    void should_ValidateCodeSuccessfully_When_CodeIsValidAndNotExpired() {
        // Given
        when(emailCodeValidationRepository.findByEmailAndCodeAndUsedFalse(VALID_EMAIL, VALID_CODE))
                .thenReturn(Optional.of(validCodeValidation));
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.of(testUser));

        // When
        boolean result = emailCodeValidationService.validateCode(VALID_EMAIL, VALID_CODE);

        // Then
        assertTrue(result);
        assertTrue(validCodeValidation.isUsed());
        assertTrue(testUser.isEmailVerified());

        verify(emailCodeValidationRepository).save(validCodeValidation);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should return false when code does not exist")
    void should_ReturnFalse_When_CodeDoesNotExist() {
        // Given
        when(emailCodeValidationRepository.findByEmailAndCodeAndUsedFalse(VALID_EMAIL, VALID_CODE))
                .thenReturn(Optional.empty());

        // When
        boolean result = emailCodeValidationService.validateCode(VALID_EMAIL, VALID_CODE);

        // Then
        assertFalse(result);
        verify(emailCodeValidationRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return false when code is expired")
    void should_ReturnFalse_When_CodeIsExpired() {
        // Given
        EmailCodeValidation expiredValidation = EmailCodeValidation.builder()
                .email(VALID_EMAIL)
                .code(VALID_CODE)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(emailCodeValidationRepository.findByEmailAndCodeAndUsedFalse(VALID_EMAIL, VALID_CODE))
                .thenReturn(Optional.of(expiredValidation));

        // When
        boolean result = emailCodeValidationService.validateCode(VALID_EMAIL, VALID_CODE);

        // Then
        assertFalse(result);
        verify(emailCodeValidationRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should validate code successfully when user does not exist")
    void should_ValidateCodeSuccessfully_When_UserDoesNotExist() {
        // Given
        when(emailCodeValidationRepository.findByEmailAndCodeAndUsedFalse(VALID_EMAIL, VALID_CODE))
                .thenReturn(Optional.of(validCodeValidation));
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.empty());

        // When
        boolean result = emailCodeValidationService.validateCode(VALID_EMAIL, VALID_CODE);

        // Then
        assertTrue(result);
        assertTrue(validCodeValidation.isUsed());

        verify(emailCodeValidationRepository).save(validCodeValidation);
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "12345", "1234567", "abcdef", "12345a"})
    @DisplayName("Should handle invalid code formats")
    void should_HandleInvalidCodeFormats_When_ValidatingCode(String invalidCode) {
        // Given
        when(emailCodeValidationRepository.findByEmailAndCodeAndUsedFalse(eq(VALID_EMAIL), eq(invalidCode)))
                .thenReturn(Optional.empty());

        // When
        boolean result = emailCodeValidationService.validateCode(VALID_EMAIL, invalidCode);

        // Then
        assertFalse(result);
        verify(emailCodeValidationRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "invalid-email", "test@", "@example.com"})
    @DisplayName("Should handle invalid email formats when validating")
    void should_HandleInvalidEmailFormats_When_ValidatingCode(String invalidEmail) {
        // Given
        when(emailCodeValidationRepository.findByEmailAndCodeAndUsedFalse(eq(invalidEmail), eq(VALID_CODE)))
                .thenReturn(Optional.empty());

        // When
        boolean result = emailCodeValidationService.validateCode(invalidEmail, VALID_CODE);

        // Then
        assertFalse(result);
        verify(emailCodeValidationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should cleanup expired codes successfully")
    void should_CleanupExpiredCodes_When_MethodCalled() {
        // When
        emailCodeValidationService.cleanupExpiredCodes();

        // Then
        verify(emailCodeValidationRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle repository error during cleanup")
    void should_HandleRepositoryError_When_CleaningUpCodes() {
        // Given
        doThrow(new RuntimeException("Database error"))
                .when(emailCodeValidationRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                emailCodeValidationService.cleanupExpiredCodes());
    }

    @Test
    @DisplayName("Should handle repository save error during validation")
    void should_HandleRepositorySaveError_When_ValidatingCode() {
        // Given
        when(emailCodeValidationRepository.findByEmailAndCodeAndUsedFalse(VALID_EMAIL, VALID_CODE))
                .thenReturn(Optional.of(validCodeValidation));
        when(emailCodeValidationRepository.save(any(EmailCodeValidation.class)))
                .thenThrow(new RuntimeException("Save error"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                emailCodeValidationService.validateCode(VALID_EMAIL, VALID_CODE));
    }

    @Test
    @DisplayName("Should handle user repository save error during validation")
    void should_HandleUserRepositorySaveError_When_ValidatingCode() {
        // Given
        when(emailCodeValidationRepository.findByEmailAndCodeAndUsedFalse(VALID_EMAIL, VALID_CODE))
                .thenReturn(Optional.of(validCodeValidation));
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("User save error"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                emailCodeValidationService.validateCode(VALID_EMAIL, VALID_CODE));
    }

    @Test
    @DisplayName("Should generate numeric code with correct length")
    void should_GenerateNumericCodeWithCorrectLength_When_GeneratingCode() {
        // Given
        when(emailCodeValidationRepository.save(any(EmailCodeValidation.class)))
                .thenReturn(validCodeValidation);

        // When
        String code = emailCodeValidationService.generateAndSendCode(VALID_EMAIL);

        // Then
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("^\\d{6}$"), "Code should be exactly 6 digits");

        // Verify all characters are digits
        for (char c : code.toCharArray()) {
            assertTrue(Character.isDigit(c), "All characters should be digits");
        }
    }

    @Test
    @DisplayName("Should set correct expiration time when generating code")
    void should_SetCorrectExpirationTime_When_GeneratingCode() {
        // Given
        LocalDateTime beforeGeneration = LocalDateTime.now();
        when(emailCodeValidationRepository.save(any(EmailCodeValidation.class)))
                .thenAnswer(invocation -> {
                    EmailCodeValidation validation = invocation.getArgument(0);
                    LocalDateTime afterGeneration = LocalDateTime.now();

                    assertTrue(validation.getExpiresAt().isAfter(beforeGeneration.plusMinutes(9)));
                    assertTrue(validation.getExpiresAt().isBefore(afterGeneration.plusMinutes(11)));

                    return validation;
                });

        // When
        emailCodeValidationService.generateAndSendCode(VALID_EMAIL);

        // Then
        verify(emailCodeValidationRepository).save(any(EmailCodeValidation.class));
    }

    @Test
    @DisplayName("Should mark code as used when validation succeeds")
    void should_MarkCodeAsUsed_When_ValidationSucceeds() {
        // Given
        EmailCodeValidation validation = EmailCodeValidation.builder()
                .email(VALID_EMAIL)
                .code(VALID_CODE)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(emailCodeValidationRepository.findByEmailAndCodeAndUsedFalse(VALID_EMAIL, VALID_CODE))
                .thenReturn(Optional.of(validation));
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.empty());

        // When
        boolean result = emailCodeValidationService.validateCode(VALID_EMAIL, VALID_CODE);

        // Then
        assertTrue(result);
        assertTrue(validation.isUsed());
        verify(emailCodeValidationRepository).save(validation);
    }
}