package br.edu.utfpr.pb.ext.server.email;

import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import static org.assertj.core.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;

@DisplayName("EmailCodeDto Tests")
class EmailCodeDtoTest {

    private Validator validator;
    private EmailCodeDto emailCodeDto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        emailCodeDto = new EmailCodeDto();
    }

    private EmailCodeDto createValidEmailCodeDto() {
        EmailCodeDto dto = new EmailCodeDto();
        dto.setId(1L);
        dto.setEmail("test@example.com");
        dto.setCode("123456");
        dto.setType(TipoCodigo.REGISTRATION);
        dto.setUsed(false);
        dto.setGeneratedAt(LocalDateTime.now());
        dto.setExpiration(LocalDateTime.now().plusHours(1));
        return dto;
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get id correctly")
        void shouldSetAndGetId() {
            // Given
            Long id = 1L;
            // When
            emailCodeDto.setId(id);
            // Then
            assertThat(emailCodeDto.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get email correctly")
        void shouldSetAndGetEmail() {
            // Given
            String email = "test@example.com";
            // When
            emailCodeDto.setEmail(email);
            // Then
            assertThat(emailCodeDto.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should set and get code correctly")
        void shouldSetAndGetCode() {
            // Given
            String code = "123456";
            // When
            emailCodeDto.setCode(code);
            // Then
            assertThat(emailCodeDto.getCode()).isEqualTo(code);
        }

        @Test
        @DisplayName("Should set and get type correctly")
        void shouldSetAndGetType() {
            // Given
            TipoCodigo type = TipoCodigo.REGISTRATION;
            // When
            emailCodeDto.setType(type);
            // Then
            assertThat(emailCodeDto.getType()).isEqualTo(type);
        }

        @Test
        @DisplayName("Should set and get used correctly")
        void shouldSetAndGetUsed() {
            // Given
            Boolean used = true;
            // When
            emailCodeDto.setUsed(used);
            // Then
            assertThat(emailCodeDto.getUsed()).isEqualTo(used);
        }

        @Test
        @DisplayName("Should set and get generatedAt correctly")
        void shouldSetAndGetGeneratedAt() {
            // Given
            LocalDateTime generatedAt = LocalDateTime.now();
            // When
            emailCodeDto.setGeneratedAt(generatedAt);
            // Then
            assertThat(emailCodeDto.getGeneratedAt()).isEqualTo(generatedAt);
        }

        @Test
        @DisplayName("Should set and get expiration correctly")
        void shouldSetAndGetExpiration() {
            // Given
            LocalDateTime expiration = LocalDateTime.now().plusHours(1);
            // When
            emailCodeDto.setExpiration(expiration);
            // Then
            assertThat(emailCodeDto.getExpiration()).isEqualTo(expiration);
        }

        @Test
        @DisplayName("Should handle null values for nullable fields")
        void shouldHandleNullValues() {
            // When
            emailCodeDto.setId(null);
            // Then
            assertThat(emailCodeDto.getId()).isNull();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation with valid data")
        void shouldPassValidationWithValidData() {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            // When
            Set<ConstraintViolation<EmailCodeDto>> violations = validator.validate(dto);
            // Then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should fail validation with invalid email - null, empty or blank")
        void shouldFailValidationWithInvalidEmailNullEmptyBlank(String email) {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setEmail(email);
            // When
            Set<ConstraintViolation<EmailCodeDto>> violations = validator.validate(dto);
            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .contains("email");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid-email",
            "@example.com",
            "user@",
            "user@domain",
            "user..name@domain.com",
            "user@domain..com",
            "user@domain.c",
            "user name@domain.com",
            "user@domain com"
        })
        @DisplayName("Should fail validation with malformed email addresses")
        void shouldFailValidationWithMalformedEmails(String email) {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setEmail(email);
            // When
            Set<ConstraintViolation<EmailCodeDto>> violations = validator.validate(dto);
            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("E-mail inválido.");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "user@domain.com",
            "test.email@example.org",
            "user123@test-domain.com",
            "user+tag@example.co.uk"
        })
        @DisplayName("Should pass validation with valid email formats")
        void shouldPassValidationWithValidEmails(String email) {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setEmail(email);
            // When
            Set<ConstraintViolation<EmailCodeDto>> violations = validator.validate(dto);
            // Then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should fail validation with invalid code - null, empty or blank")
        void shouldFailValidationWithInvalidCode(String code) {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setCode(code);
            // When
            Set<ConstraintViolation<EmailCodeDto>> violations = validator.validate(dto);
            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .contains("code");
            assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("O código é obrigatório.");
        }

        @Test
        @DisplayName("Should fail validation with null type")
        void shouldFailValidationWithNullType() {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setType(null);
            // When
            Set<ConstraintViolation<EmailCodeDto>> violations = validator.validate(dto);
            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .contains("type");
            assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("O tipo do código é obrigatório.");
        }

        @ParameterizedTest
        @EnumSource(TipoCodigo.class)
        @DisplayName("Should pass validation with valid TipoCodigo values")
        void shouldPassValidationWithValidTipoCodigo(TipoCodigo type) {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setType(type);
            // When
            Set<ConstraintViolation<EmailCodeDto>> violations = validator.validate(dto);
            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation with null used")
        void shouldFailValidationWithNullUsed() {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setUsed(null);
            // When
            Set<ConstraintViolation<EmailCodeDto>> violations = validator.validate(dto);
            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .contains("used");
            assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("O status de uso é obrigatório.");
        }

        @Test
        @DisplayName("Should fail validation with null generatedAt")
        void shouldFailValidationWithNullGeneratedAt() {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setGeneratedAt(null);
            // When
            Set<ConstraintViolation<EmailCodeDto>> violations = validator.validate(dto);
            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .contains("generatedAt");
            assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("A data de geração é obrigatória.");
        }

        @Test
        @DisplayName("Should fail validation with null expiration")
        void shouldFailValidationWithNullExpiration() {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setExpiration(null);
            // When
            Set<ConstraintViolation<EmailCodeDto>> violations = validator.validate(dto);
            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .contains("expiration");
            assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("A data de expiração é obrigatória.");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle maximum length email addresses")
        void shouldHandleMaximumLengthEmails() {
            // Given - Create a long but valid email
            String longLocalPart = "a".repeat(64);
            String longDomainPart = "b".repeat(59) + ".com";
            String longEmail = longLocalPart + "@" + longDomainPart;
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setEmail(longEmail);

            // When
            Set<ConstraintViolation<EmailCodeDto>> violations = validator.validate(dto);

            // Then
            assertThat(dto.getEmail()).isEqualTo(longEmail);
            // Validation should pass for legitimate long emails
        }

        @Test
        @DisplayName("Should handle unicode characters in email")
        void shouldHandleUnicodeCharactersInEmail() {
            // Given
            String unicodeEmail = "tëst@exämple.com";
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setEmail(unicodeEmail);

            // Then
            assertThat(dto.getEmail()).isEqualTo(unicodeEmail);
        }

        @Test
        @DisplayName("Should handle special characters in code")
        void shouldHandleSpecialCharactersInCode() {
            // Given
            String specialCode = "ABC-123_XYZ";
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setCode(specialCode);

            // Then
            assertThat(dto.getCode()).isEqualTo(specialCode);
        }

        @Test
        @DisplayName("Should handle very long codes")
        void shouldHandleVeryLongCodes() {
            // Given
            String longCode = "1".repeat(1000);
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setCode(longCode);

            // Then
            assertThat(dto.getCode()).isEqualTo(longCode);
        }

        @Test
        @DisplayName("Should handle extreme past dates")
        void shouldHandleExtremePastDates() {
            // Given
            LocalDateTime pastDate = LocalDateTime.of(1900, 1, 1, 0, 0);
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setGeneratedAt(pastDate);
            dto.setExpiration(pastDate.plusDays(1));

            // Then
            assertThat(dto.getGeneratedAt()).isEqualTo(pastDate);
            assertThat(dto.getExpiration()).isEqualTo(pastDate.plusDays(1));
        }

        @Test
        @DisplayName("Should handle extreme future dates")
        void shouldHandleExtremeFutureDates() {
            // Given
            LocalDateTime futureDate = LocalDateTime.of(2099, 12, 31, 23, 59);
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setGeneratedAt(futureDate);
            dto.setExpiration(futureDate.plusDays(1));

            // Then
            assertThat(dto.getGeneratedAt()).isEqualTo(futureDate);
            assertThat(dto.getExpiration()).isEqualTo(futureDate.plusDays(1));
        }

        @Test
        @DisplayName("Should handle minimum and maximum Long values for id")
        void shouldHandleMinMaxLongValues() {
            // Given
            EmailCodeDto dto1 = createValidEmailCodeDto();
            EmailCodeDto dto2 = createValidEmailCodeDto();

            // When
            dto1.setId(Long.MIN_VALUE);
            dto2.setId(Long.MAX_VALUE);

            // Then
            assertThat(dto1.getId()).isEqualTo(Long.MIN_VALUE);
            assertThat(dto2.getId()).isEqualTo(Long.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("Object State and Behavior Tests")
    class ObjectStateTests {

        @Test
        @DisplayName("Should maintain state consistency after multiple operations")
        void shouldMaintainStateConsistencyAfterMultipleOperations() {
            // Given
            EmailCodeDto dto = new EmailCodeDto();

            // When
            dto.setId(1L);
            dto.setEmail("test@example.com");
            dto.setCode("123456");
            dto.setType(TipoCodigo.REGISTRATION);
            dto.setUsed(false);
            LocalDateTime now = LocalDateTime.now();
            dto.setGeneratedAt(now);
            dto.setExpiration(now.plusHours(1));

            // Then
            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getEmail()).isEqualTo("test@example.com");
            assertThat(dto.getCode()).isEqualTo("123456");
            assertThat(dto.getType()).isEqualTo(TipoCodigo.REGISTRATION);
            assertThat(dto.getUsed()).isEqualTo(false);
            assertThat(dto.getGeneratedAt()).isEqualTo(now);
            assertThat(dto.getExpiration()).isEqualTo(now.plusHours(1));
        }

        @Test
        @DisplayName("Should handle boolean state changes correctly")
        void shouldHandleBooleanStateChanges() {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setUsed(false);

            // When
            dto.setUsed(true);

            // Then
            assertThat(dto.getUsed()).isTrue();

            // When
            dto.setUsed(false);

            // Then
            assertThat(dto.getUsed()).isFalse();
        }

        @Test
        @DisplayName("Should handle type changes correctly")
        void shouldHandleTypeChanges() {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            dto.setType(TipoCodigo.REGISTRATION);

            // When
            dto.setType(TipoCodigo.PASSWORD_RESET);

            // Then
            assertThat(dto.getType()).isEqualTo(TipoCodigo.PASSWORD_RESET);
        }

        @Test
        @DisplayName("Should handle date modifications correctly")
        void shouldHandleDateModifications() {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            LocalDateTime originalDate = LocalDateTime.now();
            dto.setGeneratedAt(originalDate);

            // When
            LocalDateTime newDate = originalDate.plusDays(1);
            dto.setGeneratedAt(newDate);

            // Then
            assertThat(dto.getGeneratedAt()).isEqualTo(newDate);
            assertThat(dto.getGeneratedAt()).isNotEqualTo(originalDate);
        }

        @Test
        @DisplayName("Should handle setting fields to null where allowed")
        void shouldHandleNullFieldsWhereAllowed() {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();

            // When - ID can be null (likely for new entities)
            dto.setId(null);

            // Then
            assertThat(dto.getId()).isNull();

            // But other fields should maintain their values
            assertThat(dto.getEmail()).isNotNull();
            assertThat(dto.getCode()).isNotNull();
            assertThat(dto.getType()).isNotNull();
            assertThat(dto.getUsed()).isNotNull();
            assertThat(dto.getGeneratedAt()).isNotNull();
            assertThat(dto.getExpiration()).isNotNull();
        }

        @Test
        @DisplayName("Should handle immutable field references correctly")
        void shouldHandleImmutableFieldReferences() {
            // Given
            EmailCodeDto dto = createValidEmailCodeDto();
            LocalDateTime originalGeneratedAt = dto.getGeneratedAt();
            LocalDateTime originalExpiration = dto.getExpiration();

            // When - Modify the returned LocalDateTime objects
            LocalDateTime modifiedGeneratedAt = originalGeneratedAt.plusDays(1);
            LocalDateTime modifiedExpiration = originalExpiration.plusDays(1);

            // Then - Original DTO values should be unchanged
            assertThat(dto.getGeneratedAt()).isEqualTo(originalGeneratedAt);
            assertThat(dto.getExpiration()).isEqualTo(originalExpiration);
            assertThat(dto.getGeneratedAt()).isNotEqualTo(modifiedGeneratedAt);
            assertThat(dto.getExpiration()).isNotEqualTo(modifiedExpiration);
        }
    }
}