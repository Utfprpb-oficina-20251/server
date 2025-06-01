package br.edu.utfpr.pb.ext.server.email;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Serviço responsável por validar códigos enviados por e-mail. */
@Service
public class EmailCodeValidationService {

  private final EmailCodeRepository repository;

  /****
   * Cria uma instância do serviço de validação de códigos de e-mail.
   *
   * @param repository repositório utilizado para acessar os códigos de e-mail
   */
  public EmailCodeValidationService(EmailCodeRepository repository) {
    this.repository = repository;
  }

  /**
   * Valida se o código fornecido para um e-mail e tipo é o mais recente, corresponde ao valor esperado, não foi utilizado e está dentro do prazo de validade.
   * Se todas as condições forem atendidas, marca o código como utilizado e salva a alteração.
   *
   * @param email endereço de e-mail ao qual o código está associado
   * @param type tipo do código (exemplo: cadastro, recuperação)
   * @param code código informado para validação
   * @return true se o código for válido e marcado como utilizado; false caso contrário
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
