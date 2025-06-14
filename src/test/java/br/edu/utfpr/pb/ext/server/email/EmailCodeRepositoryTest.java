package br.edu.utfpr.pb.ext.server.email;

import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("EmailCodeRepository Tests")
class EmailCodeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmailCodeRepository emailCodeRepository;

    private String testEmail;
    private String testCode;
    private LocalDateTime now;
    private LocalDateTime futureTime;
    private LocalDateTime pastTime;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testCode = "123456";
        now = LocalDateTime.now();
        futureTime = now.plusMinutes(10);
        pastTime = now.minusMinutes(10);

        // Clear any existing data
        emailCodeRepository.deleteAll();
        entityManager.flush();
    }

    private EmailCode createEmailCode(String email, String code, TipoCodigo type,
                                      LocalDateTime generatedAt, LocalDateTime expiration, boolean used) {
        EmailCode emailCode = new EmailCode(email, code, generatedAt, expiration, type);
        emailCode.setUsed(used);
        return emailCode;
    }

    private EmailCode saveEmailCode(String email, String code, TipoCodigo type,
                                    LocalDateTime generatedAt, LocalDateTime expiration, boolean used) {
        EmailCode emailCode = createEmailCode(email, code, type, generatedAt, expiration, used);
        return entityManager.persistAndFlush(emailCode);
    }

    @Nested
    @DisplayName("findTopByEmailAndTypeOrderByGeneratedAtDesc Tests")
    class FindTopByEmailAndTypeTests {

        @Test
        @DisplayName("Should find most recent email code by email and type")
        void shouldFindMostRecentEmailCode() {
            // Given
            LocalDateTime older = now.minusMinutes(5);
            LocalDateTime newer = now.minusMinutes(1);

            saveEmailCode(testEmail, "111111", TipoCodigo.CADASTRO, older, futureTime, false);
            EmailCode expected = saveEmailCode(testEmail, "222222", TipoCodigo.CADASTRO, newer, futureTime, false);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findTopByEmailAndTypeOrderByGeneratedAtDesc(testEmail, TipoCodigo.CADASTRO);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("222222");
            assertThat(result.get().getGeneratedAt()).isEqualTo(newer);
            assertThat(result.get().getId()).isEqualTo(expected.getId());
        }

        @Test
        @DisplayName("Should return empty when no email code found for email and type")
        void shouldReturnEmptyWhenNoEmailCodeFound() {
            // Given
            saveEmailCode(testEmail, testCode, TipoCodigo.RECUPERACAO, now, futureTime, false);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findTopByEmailAndTypeOrderByGeneratedAtDesc(testEmail, TipoCodigo.CADASTRO);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find correct email code when multiple emails exist")
        void shouldFindCorrectEmailCodeAmongMultipleEmails() {
            // Given
            saveEmailCode("other@example.com", "111111", TipoCodigo.CADASTRO, now, futureTime, false);
            EmailCode expected = saveEmailCode(testEmail, "222222", TipoCodigo.CADASTRO, now, futureTime, false);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findTopByEmailAndTypeOrderByGeneratedAtDesc(testEmail, TipoCodigo.CADASTRO);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(testEmail);
            assertThat(result.get().getCode()).isEqualTo("222222");
        }

        @Test
        @DisplayName("Should handle null email parameter")
        void shouldHandleNullEmail() {
            // Given
            saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO, now, futureTime, false);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findTopByEmailAndTypeOrderByGeneratedAtDesc(null, TipoCodigo.CADASTRO);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null type parameter")
        void shouldHandleNullType() {
            // Given
            saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO, now, futureTime, false);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findTopByEmailAndTypeOrderByGeneratedAtDesc(testEmail, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find most recent among used and unused codes")
        void shouldFindMostRecentRegardlessOfUsedStatus() {
            // Given
            LocalDateTime older = now.minusMinutes(5);
            LocalDateTime newer = now.minusMinutes(1);

            saveEmailCode(testEmail, "111111", TipoCodigo.CADASTRO, older, futureTime, false);
            EmailCode expected = saveEmailCode(testEmail, "222222", TipoCodigo.CADASTRO, newer, futureTime, true);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findTopByEmailAndTypeOrderByGeneratedAtDesc(testEmail, TipoCodigo.CADASTRO);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("222222");
            assertThat(result.get().isUsed()).isTrue();
        }
    }

    @Nested
    @DisplayName("findByCodeAndExpirationAfterAndUsedFalse Tests")
    class FindByCodeAndExpirationAfterAndUsedFalseTests {

        @Test
        @DisplayName("Should find valid unused non-expired code")
        void shouldFindValidUnusedNonExpiredCode() {
            // Given
            EmailCode expected = saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO,
                now.minusMinutes(5), futureTime, false);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findByCodeAndExpirationAfterAndUsedFalse(testCode, now);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo(testCode);
            assertThat(result.get().getEmail()).isEqualTo(testEmail);
            assertThat(result.get().isUsed()).isFalse();
            assertThat(result.get().getExpiration()).isAfter(now);
            assertThat(result.get().getId()).isEqualTo(expected.getId());
        }

        @Test
        @DisplayName("Should not find expired code")
        void shouldNotFindExpiredCode() {
            // Given
            saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO,
                now.minusMinutes(10), pastTime, false);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findByCodeAndExpirationAfterAndUsedFalse(testCode, now);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should not find used code")
        void shouldNotFindUsedCode() {
            // Given
            saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO,
                now.minusMinutes(5), futureTime, true);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findByCodeAndExpirationAfterAndUsedFalse(testCode, now);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should not find non-existent code")
        void shouldNotFindNonExistentCode() {
            // Given
            saveEmailCode(testEmail, "different-code", TipoCodigo.CADASTRO,
                now.minusMinutes(5), futureTime, false);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findByCodeAndExpirationAfterAndUsedFalse(testCode, now);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null code parameter")
        void shouldHandleNullCode() {
            // Given
            saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO,
                now.minusMinutes(5), futureTime, false);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findByCodeAndExpirationAfterAndUsedFalse(null, now);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null datetime parameter")
        void shouldHandleNullDateTime() {
            // Given
            saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO,
                now.minusMinutes(5), futureTime, false);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findByCodeAndExpirationAfterAndUsedFalse(testCode, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find code that expires exactly at the boundary")
        void shouldFindCodeExpiringAtBoundary() {
            // Given
            LocalDateTime exactExpiration = now.plusSeconds(1);
            saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO,
                now.minusMinutes(5), exactExpiration, false);

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findByCodeAndExpirationAfterAndUsedFalse(testCode, now);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getExpiration()).isAfter(now);
        }

        @Test
        @DisplayName("Should not find code when multiple codes exist but all are used or expired")
        void shouldNotFindWhenAllCodesAreUsedOrExpired() {
            // Given
            saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO,
                now.minusMinutes(10), pastTime, false); // expired
            saveEmailCode("other@example.com", testCode, TipoCodigo.RECUPERACAO,
                now.minusMinutes(5), futureTime, true); // used

            // When
            Optional<EmailCode> result = emailCodeRepository
                .findByCodeAndExpirationAfterAndUsedFalse(testCode, now);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByEmailAndTypeAndGeneratedAtAfter Tests")
    class CountByEmailAndTypeAndGeneratedAtAfterTests {

        @Test
        @DisplayName("Should count codes generated after specified time")
        void shouldCountCodesGeneratedAfterSpecifiedTime() {
            // Given
            LocalDateTime threshold = now.minusMinutes(5);

            saveEmailCode(testEmail, "111111", TipoCodigo.CADASTRO,
                now.minusMinutes(10), futureTime, false); // before threshold
            saveEmailCode(testEmail, "222222", TipoCodigo.CADASTRO,
                now.minusMinutes(3), futureTime, false); // after threshold
            saveEmailCode(testEmail, "333333", TipoCodigo.CADASTRO,
                now.minusMinutes(1), futureTime, true); // after threshold, used

            // When
            Long count = emailCodeRepository
                .countByEmailAndTypeAndGeneratedAtAfter(testEmail, TipoCodigo.CADASTRO, threshold);

            // Then
            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should return zero when no codes found after specified time")
        void shouldReturnZeroWhenNoCodesFoundAfterTime() {
            // Given
            LocalDateTime threshold = now.minusMinutes(1);

            saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO,
                now.minusMinutes(10), futureTime, false);
            saveEmailCode(testEmail, "222222", TipoCodigo.CADASTRO,
                now.minusMinutes(5), futureTime, false);

            // When
            Long count = emailCodeRepository
                .countByEmailAndTypeAndGeneratedAtAfter(testEmail, TipoCodigo.CADASTRO, threshold);

            // Then
            assertThat(count).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should count only codes for specified email")
        void shouldCountOnlyCodesForSpecifiedEmail() {
            // Given
            LocalDateTime threshold = now.minusMinutes(5);

            saveEmailCode(testEmail, "111111", TipoCodigo.CADASTRO,
                now.minusMinutes(3), futureTime, false);
            saveEmailCode("other@example.com", "222222", TipoCodigo.CADASTRO,
                now.minusMinutes(3), futureTime, false);
            saveEmailCode(testEmail, "333333", TipoCodigo.CADASTRO,
                now.minusMinutes(1), futureTime, false);

            // When
            Long count = emailCodeRepository
                .countByEmailAndTypeAndGeneratedAtAfter(testEmail, TipoCodigo.CADASTRO, threshold);

            // Then
            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should count only codes for specified type")
        void shouldCountOnlyCodesForSpecifiedType() {
            // Given
            LocalDateTime threshold = now.minusMinutes(5);

            saveEmailCode(testEmail, "111111", TipoCodigo.CADASTRO,
                now.minusMinutes(3), futureTime, false);
            saveEmailCode(testEmail, "222222", TipoCodigo.RECUPERACAO,
                now.minusMinutes(3), futureTime, false);
            saveEmailCode(testEmail, "333333", TipoCodigo.CADASTRO,
                now.minusMinutes(1), futureTime, false);

            // When
            Long count = emailCodeRepository
                .countByEmailAndTypeAndGeneratedAtAfter(testEmail, TipoCodigo.CADASTRO, threshold);

            // Then
            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should handle null email parameter")
        void shouldHandleNullEmailParameter() {
            // Given
            LocalDateTime threshold = now.minusMinutes(5);
            saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO,
                now.minusMinutes(3), futureTime, false);

            // When
            Long count = emailCodeRepository
                .countByEmailAndTypeAndGeneratedAtAfter(null, TipoCodigo.CADASTRO, threshold);

            // Then
            assertThat(count).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle null type parameter")
        void shouldHandleNullTypeParameter() {
            // Given
            LocalDateTime threshold = now.minusMinutes(5);
            saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO,
                now.minusMinutes(3), futureTime, false);

            // When
            Long count = emailCodeRepository
                .countByEmailAndTypeAndGeneratedAtAfter(testEmail, null, threshold);

            // Then
            assertThat(count).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle null datetime parameter")
        void shouldHandleNullDateTimeParameter() {
            // Given
            saveEmailCode(testEmail, testCode, TipoCodigo.CADASTRO,
                now.minusMinutes(3), futureTime, false);

            // When
            Long count = emailCodeRepository
                .countByEmailAndTypeAndGeneratedAtAfter(testEmail, TipoCodigo.CADASTRO, null);

            // Then
            assertThat(count).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should exclude codes generated exactly at threshold time")
        void shouldExcludeCodesGeneratedExactlyAtThresholdTime() {
            // Given
            LocalDateTime exactThreshold = now.minusMinutes(5);

            saveEmailCode(testEmail, "111111", TipoCodigo.CADASTRO,
                exactThreshold, futureTime, false); // exactly at threshold
            saveEmailCode(testEmail, "222222", TipoCodigo.CADASTRO,
                exactThreshold.plusSeconds(1), futureTime, false); // after threshold

            // When
            Long count = emailCodeRepository
                .countByEmailAndTypeAndGeneratedAtAfter(testEmail, TipoCodigo.CADASTRO, exactThreshold);

            // Then
            assertThat(count).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should count both used and unused codes")
        void shouldCountBothUsedAndUnusedCodes() {
            // Given
            LocalDateTime threshold = now.minusMinutes(5);

            saveEmailCode(testEmail, "111111", TipoCodigo.CADASTRO,
                now.minusMinutes(3), futureTime, false); // unused
            saveEmailCode(testEmail, "222222", TipoCodigo.CADASTRO,
                now.minusMinutes(2), futureTime, true); // used
            saveEmailCode(testEmail, "333333", TipoCodigo.CADASTRO,
                now.minusMinutes(1), futureTime, false); // unused

            // When
            Long count = emailCodeRepository
                .countByEmailAndTypeAndGeneratedAtAfter(testEmail, TipoCodigo.CADASTRO, threshold);

            // Then
            assertThat(count).isEqualTo(3L);
        }
    }
}