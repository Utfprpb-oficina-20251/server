package br.edu.utfpr.pb.ext.server.repository;

import br.edu.utfpr.pb.ext.server.model.EmailCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmailCodeRepositoryTest {

    @Autowired
    private EmailCodeRepository repository;

    @Test
    void testFindAllByEmailAndTypeAndGeneratedAtAfter() {
        // Arrange
        EmailCode code = new EmailCode();
        code.setEmail("teste@utfpr.edu.br");
        code.setCode("ABC123");
        code.setType("cadastro");
        code.setGeneratedAt(LocalDateTime.now().minusMinutes(10));
        code.setExpiration(LocalDateTime.now().plusMinutes(10));
        code.setUsed(false);

        repository.save(code);

        // Act
        List<EmailCode> results = repository.findAllByEmailAndTypeAndGeneratedAtAfter(
                "teste@utfpr.edu.br", "cadastro", LocalDateTime.now().minusHours(1)
        );

        // Assert
        assertEquals(1, results.size());
        assertEquals("ABC123", results.get(0).getCode());
    }
}