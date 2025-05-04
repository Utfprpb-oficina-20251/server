package br.edu.utfpr.pb.ext.server.repository;

import br.edu.utfpr.pb.ext.server.model.EmailCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositório para operações com EmailCode no banco de dados. */
public interface EmailCodeRepository extends JpaRepository<EmailCode, Long> {

  /** Retorna o código mais recente para um e-mail e tipo específico. */
  Optional<EmailCode> findTopByEmailAndTypeOrderByGeneratedAtDesc(String email, String type);

  /** Verifica se o código ainda é válido (não expirado e não usado). */
  Optional<EmailCode> findByCodeAndExpirationAfterAndUsedFalse(String code, LocalDateTime now);

  List<EmailCode> findAllByEmailAndTypeAndGeneratedAtAfter(String email, String type, LocalDateTime generatedAt);
}
