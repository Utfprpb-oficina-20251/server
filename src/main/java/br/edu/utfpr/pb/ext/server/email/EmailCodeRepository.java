package br.edu.utfpr.pb.ext.server.email;

import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositório para operações com EmailCode no banco de dados. */
public interface EmailCodeRepository extends JpaRepository<EmailCode, Long> {

  /**
 * Busca o código de e-mail mais recente para o e-mail e tipo informados, considerando a data de geração em ordem decrescente.
 *
 * @param email endereço de e-mail a ser consultado
 * @param type tipo do código de e-mail
 * @return um {@link Optional} contendo o código mais recente, se existir
 */
  Optional<EmailCode> findTopByEmailAndTypeOrderByGeneratedAtDesc(String email, TipoCodigo type);

  /**
   * Retorna um código de e-mail correspondente ao valor informado, que ainda não expirou e não foi
   * utilizado.
   *
   * @param code valor do código de verificação a ser buscado
   * @param now data e hora limite para considerar o código como não expirado
   * @return um Optional contendo o EmailCode válido, se encontrado
   */
  Optional<EmailCode> findByCodeAndExpirationAfterAndUsedFalse(String code, LocalDateTime now);

  /**
       * Conta a quantidade de códigos de e-mail de um determinado tipo associados a um endereço, gerados estritamente após a data e hora especificadas.
       *
       * @param email endereço de e-mail a ser consultado
       * @param type tipo do código de e-mail
       * @param generatedAt data e hora limite exclusiva para considerar os códigos gerados posteriormente
       * @return número de códigos de e-mail encontrados conforme os critérios
       */
  Long countByEmailAndTypeAndGeneratedAtAfter(
      String email, TipoCodigo type, LocalDateTime generatedAt);
}
