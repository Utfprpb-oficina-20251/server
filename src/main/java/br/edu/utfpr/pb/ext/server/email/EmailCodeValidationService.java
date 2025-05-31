package br.edu.utfpr.pb.ext.server.email;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Serviço responsável por validar códigos enviados por e-mail. */
@Service
public class EmailCodeValidationService {

  private final EmailCodeRepository repository;

  public EmailCodeValidationService(EmailCodeRepository repository) {
    this.repository = repository;
  }

  /**
   * Valida se o código informado é o mais recente, corresponde ao tipo, não foi utilizado e está
   * válido. Se for válido, marca como usado e salva.
   *
   * @param email endereço de e-mail
   * @param type tipo do código (ex: cadastro, recuperação)
   * @param code código informado pelo usuário
   * @return true se válido, false se inválido ou expirado
   */
  @Transactional
  public boolean validateCode(String email, String type, String code) {
    return repository
        .findTopByEmailAndTypeOrderByGeneratedAtDesc(email, type)
        .filter(ec -> ec.getCode().equals(code))
        .filter(ec -> !ec.isUsed())
        .filter(ec -> ec.getExpiration().isAfter(LocalDateTime.now()))
        .map(
            ec -> {
              ec.setUsed(true);
              repository.save(ec);
              return true;
            })
        .orElse(false);
  }
}
