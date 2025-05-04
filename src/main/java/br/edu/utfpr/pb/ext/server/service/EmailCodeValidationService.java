package br.edu.utfpr.pb.ext.server.service;

import br.edu.utfpr.pb.ext.server.model.EmailCode;
import br.edu.utfpr.pb.ext.server.repository.EmailCodeRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Serviço responsável por validar códigos enviados por e-mail. */
@Service
public class EmailCodeValidationService {

  private final EmailCodeRepository emailCodeRepository;

  public EmailCodeValidationService(EmailCodeRepository emailCodeRepository) {
    this.emailCodeRepository = emailCodeRepository;
  }

  /**
   * Valida se o código informado é válido, ainda está no prazo e não foi usado. Se for válido,
   * marca o código como usado.
   *
   * @param email E-mail para o qual o código foi enviado
   * @param type Tipo do código ("cadastro", "recuperacao", etc.)
   * @param code Código informado pelo usuário
   * @return true se for válido, false caso contrário
   */
  public boolean validateCode(String email, String type, String code) {
    Optional<EmailCode> optional =
        emailCodeRepository.findTopByEmailAndTypeOrderByGeneratedAtDesc(email, type);

    if (optional.isEmpty()) return false;

    EmailCode emailCode = optional.get();
    boolean valido =
        emailCode.getCode().equals(code)
            && !emailCode.isUsed()
            && emailCode.getExpiration().isAfter(LocalDateTime.now());

    if (valido) {
      emailCode.setUsed(true);
      emailCodeRepository.save(emailCode);
    }

    return valido;
  }
}
