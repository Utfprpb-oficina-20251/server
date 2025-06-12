package br.edu.utfpr.pb.ext.server.email;

import java.time.LocalDateTime;
import java.util.Optional;

import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositório para operações com EmailCode no banco de dados. */
public interface EmailCodeRepository extends JpaRepository<EmailCode, Long> {

  /**
   * Retorna o código de e-mail mais recente para o e-mail e tipo especificados, ordenado pela data
   * de geração decrescente.
   *
   * @param email endereço de e-mail a ser pesquisado
   * @param type tipo do código de e-mail
   * @return um {@link Optional} com o código mais recente, se encontrado
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
   * Conta quantos códigos de e-mail existem para um endereço e tipo específicos, gerados após a
   * data e hora informadas.
   *
   * @param email endereço de e-mail a ser considerado
   * @param type tipo do código de e-mail
   * @param generatedAt data e hora a partir da qual os códigos devem ter sido gerados (exclusivo)
   * @return quantidade de códigos de e-mail que atendem aos critérios especificados
   */
  Long countByEmailAndTypeAndGeneratedAtAfter(String email, TipoCodigo type, LocalDateTime generatedAt);
}
