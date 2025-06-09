package br.edu.utfpr.pb.ext.server.email;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
  Optional<EmailCode> findTopByEmailAndTypeOrderByGeneratedAtDesc(String email, String type);

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
   * Busca todos os códigos de e-mail associados a um endereço e tipo específicos, gerados após a
   * data e hora informadas.
   *
   * @param email endereço de e-mail para filtrar os códigos
   * @param type tipo do código de e-mail
   * @param generatedAt data e hora a partir da qual os códigos devem ter sido gerados (exclusivo)
   * @return lista de códigos de e-mail que atendem aos critérios especificados
   */
  List<EmailCode> findAllByEmailAndTypeAndGeneratedAtAfter(
      String email, String type, LocalDateTime generatedAt);
}
