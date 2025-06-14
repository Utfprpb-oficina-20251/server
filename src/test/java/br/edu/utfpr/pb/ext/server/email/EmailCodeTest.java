package br.edu.utfpr.pb.ext.server.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EmailCodeTest {

    private EmailCode emailCode;
    private LocalDateTime futureTime;
    private LocalDateTime pastTime;

    @BeforeEach
    void setUp() {
        futureTime = LocalDateTime.now().plusMinutes(15);
        pastTime = LocalDateTime.now().minusMinutes(15);
        emailCode = new EmailCode("test@example.com", "123456", futureTime);
    }

    @Test
    @DisplayName("Should create EmailCode with default constructor")
    void testDefaultConstructor() {
        EmailCode emailCode = new EmailCode();

        assertNotNull(emailCode);
        assertNull(emailCode.getId());
        assertNull(emailCode.getEmail());
        assertNull(emailCode.getCode());
        assertNull(emailCode.getCreatedAt());
        assertNull(emailCode.getExpiresAt());
        assertFalse(emailCode.isUsed());
    }

    @Test
    @DisplayName("Should create EmailCode with all arguments constructor")
    void testAllArgsConstructor() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        EmailCode emailCode = new EmailCode(1L, "test@example.com", "123456",
                createdAt, expiresAt, false);

        assertEquals(1L, emailCode.getId());
        assertEquals("test@example.com", emailCode.getEmail());
        assertEquals("123456", emailCode.getCode());
        assertEquals(createdAt, emailCode.getCreatedAt());
        assertEquals(expiresAt, emailCode.getExpiresAt());
        assertFalse(emailCode.isUsed());
    }

    @Test
    @DisplayName("Should create EmailCode with custom constructor and set defaults")
    void testCustomConstructor() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        EmailCode emailCode = new EmailCode("user@example.com", "654321", expiresAt);

        assertEquals("user@example.com", emailCode.getEmail());
        assertEquals("654321", emailCode.getCode());
        assertEquals(expiresAt, emailCode.getExpiresAt());
        assertFalse(emailCode.isUsed());
        assertNotNull(emailCode.getCreatedAt());
        assertTrue(emailCode.getCreatedAt().isAfter(beforeCreation));
        assertTrue(emailCode.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should return false when code is not expired")
    void testIsExpired_WhenNotExpired_ShouldReturnFalse() {
        LocalDateTime futureExpiration = LocalDateTime.now().plusMinutes(10);
        EmailCode emailCode = new EmailCode("test@example.com", "123456", futureExpiration);

        assertFalse(emailCode.isExpired(), "Code should not be expired when expiration is in the future");
    }

    @Test
    @DisplayName("Should return true when code is expired")
    void testIsExpired_WhenExpired_ShouldReturnTrue() {
        LocalDateTime pastExpiration = LocalDateTime.now().minusMinutes(10);
        EmailCode emailCode = new EmailCode("test@example.com", "123456", pastExpiration);

        assertTrue(emailCode.isExpired(), "Code should be expired when expiration is in the past");
    }

    @Test
    @DisplayName("Should return true when code expires exactly now")
    void testIsExpired_WhenExpiresNow_ShouldReturnTrue() {
        LocalDateTime now = LocalDateTime.now();
        EmailCode emailCode = new EmailCode("test@example.com", "123456", now);

        // Allow for small timing differences
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue(emailCode.isExpired(), "Code should be expired when expiration time has passed");
    }

    @Test
    @DisplayName("Should return true when code is not used and not expired")
    void testIsValid_WhenNotUsedAndNotExpired_ShouldReturnTrue() {
        assertTrue(emailCode.isValid(), "Code should be valid when not used and not expired");
    }

    @Test
    @DisplayName("Should return false when code is used but not expired")
    void testIsValid_WhenUsedButNotExpired_ShouldReturnFalse() {
        emailCode.setUsed(true);

        assertFalse(emailCode.isValid(), "Code should not be valid when already used");
    }

    @Test
    @DisplayName("Should return false when code is not used but expired")
    void testIsValid_WhenNotUsedButExpired_ShouldReturnFalse() {
        EmailCode expiredCode = new EmailCode("test@example.com", "123456", pastTime);

        assertFalse(expiredCode.isValid(), "Code should not be valid when expired");
    }

    @Test
    @DisplayName("Should return false when code is both used and expired")
    void testIsValid_WhenUsedAndExpired_ShouldReturnFalse() {
        EmailCode invalidCode = new EmailCode("test@example.com", "123456", pastTime);
        invalidCode.setUsed(true);

        assertFalse(invalidCode.isValid(), "Code should not be valid when both used and expired");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test@example.com",
            "user.name@domain.com",
            "user+tag@example.org",
            "user123@test-domain.co.uk",
            "a@b.co"
    })
    @DisplayName("Should accept valid email formats")
    void testValidEmails(String email) {
        EmailCode emailCode = new EmailCode(email, "123456", futureTime);

        assertEquals(email, emailCode.getEmail());
        assertNotNull(emailCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "000000",
            "123456",
            "999999",
            "012345",
            "567890"
    })
    @DisplayName("Should accept various numeric codes")
    void testValidCodes(String code) {
        EmailCode emailCode = new EmailCode("test@example.com", code, futureTime);

        assertEquals(code, emailCode.getCode());
        assertNotNull(emailCode);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should handle null and empty email values")
    void testNullAndEmptyEmails(String email) {
        EmailCode emailCode = new EmailCode(email, "123456", futureTime);

        assertEquals(email, emailCode.getEmail());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should handle null and empty code values")
    void testNullAndEmptyCodes(String code) {
        EmailCode emailCode = new EmailCode("test@example.com", code, futureTime);

        assertEquals(code, emailCode.getCode());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        EmailCode emailCode1 = new EmailCode("test@example.com", "123456", futureTime);
        emailCode1.setId(1L);

        EmailCode emailCode2 = new EmailCode("test@example.com", "123456", futureTime);
        emailCode2.setId(1L);

        EmailCode emailCode3 = new EmailCode("different@example.com", "654321", futureTime);
        emailCode3.setId(2L);

        assertEquals(emailCode1, emailCode2);
        assertEquals(emailCode1.hashCode(), emailCode2.hashCode());
        assertNotEquals(emailCode1, emailCode3);
        assertNotEquals(emailCode1.hashCode(), emailCode3.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        emailCode.setId(1L);

        String toString = emailCode.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("123456"));
        assertTrue(toString.contains("1"));
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void testSettersAndGetters() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        emailCode.setId(100L);
        emailCode.setEmail("updated@example.com");
        emailCode.setCode("999888");
        emailCode.setCreatedAt(createdAt);
        emailCode.setExpiresAt(expiresAt);
        emailCode.setUsed(true);

        assertEquals(100L, emailCode.getId());
        assertEquals("updated@example.com", emailCode.getEmail());
        assertEquals("999888", emailCode.getCode());
        assertEquals(createdAt, emailCode.getCreatedAt());
        assertEquals(expiresAt, emailCode.getExpiresAt());
        assertTrue(emailCode.isUsed());
    }

    @Test
    @DisplayName("Should handle extreme future expiration dates")
    void testExtremeFutureExpiration() {
        LocalDateTime veryFarFuture = LocalDateTime.now().plusYears(100);
        EmailCode emailCode = new EmailCode("test@example.com", "123456", veryFarFuture);

        assertFalse(emailCode.isExpired());
        assertTrue(emailCode.isValid());
    }

    @Test
    @DisplayName("Should handle extreme past expiration dates")
    void testExtremePastExpiration() {
        LocalDateTime veryFarPast = LocalDateTime.now().minusYears(100);
        EmailCode emailCode = new EmailCode("test@example.com", "123456", veryFarPast);

        assertTrue(emailCode.isExpired());
        assertFalse(emailCode.isValid());
    }

    @ParameterizedTest
    @CsvSource({
            "1, true",
            "0, false",
            "2, true",
            "60, true",
            "1440, true"
    })
    @DisplayName("Should correctly determine expiration for various minute offsets")
    void testExpirationWithVariousOffsets(int minuteOffset, boolean shouldBeValid) {
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(minuteOffset);
        EmailCode emailCode = new EmailCode("test@example.com", "123456", expirationTime);

        assertEquals(shouldBeValid, emailCode.isValid());
        assertEquals(!shouldBeValid, emailCode.isExpired());
    }

    @Test
    @DisplayName("Should handle state transitions correctly")
    void testStateTransitions() {
        // Initially valid
        assertTrue(emailCode.isValid());
        assertFalse(emailCode.isUsed());
        assertFalse(emailCode.isExpired());

        // Mark as used
        emailCode.setUsed(true);
        assertFalse(emailCode.isValid());
        assertTrue(emailCode.isUsed());
        assertFalse(emailCode.isExpired());

        // Mark as unused again
        emailCode.setUsed(false);
        assertTrue(emailCode.isValid());
        assertFalse(emailCode.isUsed());
        assertFalse(emailCode.isExpired());
    }

    @Test
    @DisplayName("Should handle null expiration date gracefully")
    void testNullExpirationDate() {
        EmailCode emailCode = new EmailCode();
        emailCode.setEmail("test@example.com");
        emailCode.setCode("123456");
        emailCode.setExpiresAt(null);
        emailCode.setUsed(false);

        // This should throw NullPointerException when checking isExpired()
        assertThrows(NullPointerException.class, emailCode::isExpired);
        assertThrows(NullPointerException.class, emailCode::isValid);
    }
}