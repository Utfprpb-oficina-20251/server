package br.edu.utfpr.pb.ext.server.repository;

import br.edu.utfpr.pb.ext.server.model.EmailCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositório para operações com EmailCode no banco de dados. */
public interface EmailCodeRepository extends JpaRepository<EmailCode, Long> {

  /**
 * Recupera o código de e-mail mais recente para um determinado e-mail e tipo.
 *
 * @param email endereço de e-mail a ser consultado
 * @param type tipo do código de e-mail
 * @return um {@link Optional} contendo o código mais recente, se existir
 */
  Optional<EmailCode> findTopByEmailAndTypeOrderByGeneratedAtDesc(String email, String type);

  /**
 * Busca um código de e-mail que corresponda ao valor informado, que ainda não expirou e não foi utilizado.
 *
 * @param code o código de verificação a ser buscado
 * @param now data e hora de referência para verificar a expiração
 * @return um Optional contendo o EmailCode válido, caso exista
 */
  Optional<EmailCode> findByCodeAndExpirationAfterAndUsedFalse(String code, LocalDateTime now);

  /**
 * Retorna uma lista de códigos de e-mail filtrados pelo e-mail, tipo e data de geração posterior à informada.
 *
 * @param email endereço de e-mail associado aos códigos
 * @param type tipo do código de e-mail
 * @param generatedAt data e hora limite; apenas códigos gerados após este momento serão retornados
 * @return lista de códigos de e-mail correspondentes aos critérios
 */
List<EmailCode> findAllByEmailAndTypeAndGeneratedAtAfter(String email, String type, LocalDateTime generatedAt);
}
