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

  /**
   * Cria uma instância do serviço de validação de códigos de e-mail com o repositório fornecido.
   *
   * @param emailCodeRepository repositório utilizado para acessar os códigos de e-mail
   */
  public EmailCodeValidationService(EmailCodeRepository emailCodeRepository) {
    this.emailCodeRepository = emailCodeRepository;
  }

  /**
   * Valida se o código de e-mail informado é o mais recente, corresponde ao tipo solicitado, não foi utilizado e está dentro do prazo de validade.
   * Se todas as condições forem atendidas, marca o código como usado e retorna true.
   *
   * @param email Endereço de e-mail para o qual o código foi enviado.
   * @param type Tipo do código (por exemplo, "cadastro", "recuperacao").
   * @param code Código informado pelo usuário.
   * @return true se o código for válido e atualizado com sucesso; false caso contrário.
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
