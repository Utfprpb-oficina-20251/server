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
   * Valida se o código informado para um e-mail e tipo é o mais recente, corresponde ao valor esperado, não foi utilizado e está dentro do prazo de validade.
   * Caso todas as condições sejam atendidas, marca o código como utilizado e persiste a alteração.
   *
   * @param email endereço de e-mail associado ao código
   * @param type tipo do código (por exemplo: cadastro, recuperação)
   * @param code código informado pelo usuário
   * @return true se o código for válido e atualizado com sucesso; false caso contrário
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
