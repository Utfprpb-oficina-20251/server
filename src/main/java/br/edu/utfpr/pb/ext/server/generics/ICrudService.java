package br.edu.utfpr.pb.ext.server.generics;

import java.io.Serializable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface ICrudService<T, I extends Serializable> {

  /**
 * Retorna todas as entidades persistidas do tipo T.
 *
 * @return lista com todas as entidades encontradas
 */
  List<T> findAll();

  /**
 * Recupera todas as entidades do tipo T, aplicando o critério de ordenação especificado.
 *
 * @param sort critério de ordenação utilizado para ordenar os resultados
 * @return lista de entidades ordenadas conforme o sort informado
 */
  List<T> findAll(Sort sort);

  /**
 * Recupera uma página de entidades com base nos critérios de paginação e ordenação especificados.
 *
 * @param pageable informações sobre a página solicitada e critérios de ordenação
 * @return uma página contendo as entidades encontradas
 */
  Page<T> findAll(Pageable pageable);

  /**
 * Salva a entidade especificada e retorna a instância persistida.
 *
 * @param entity entidade a ser salva
 * @return a entidade persistida, podendo conter alterações realizadas pelo processo de salvamento, como preenchimento de campos gerados automaticamente
 */
  T save(T entity);

  /**
 * Salva a entidade especificada e garante que todas as alterações pendentes sejam imediatamente gravadas no banco de dados.
 *
 * @param entity entidade a ser persistida
 * @return a instância persistida da entidade
 */
  T saveAndFlush(T entity);

  /**
 * Persiste uma coleção de entidades e retorna as instâncias resultantes.
 *
 * @param iterable coleção de entidades a serem persistidas
 * @return entidades persistidas
 */
  Iterable<T> save(Iterable<T> iterable);

  /**
 * Sincroniza imediatamente todas as alterações pendentes das entidades com o banco de dados.
 *
 * Utilizado para garantir que operações de escrita sejam efetivadas sem aguardar o ciclo automático de persistência.
 */
  void flush();

  /**
 * Busca uma entidade pelo seu identificador exclusivo.
 *
 * @param i identificador único da entidade
 * @return a entidade correspondente ao identificador, ou {@code null} se não encontrada
 */
  T findOne(I i);

  /**
 * Verifica se existe uma entidade persistida com o identificador informado.
 *
 * @param i identificador único da entidade
 * @return true se uma entidade com o identificador existir, false caso contrário
 */
  boolean exists(I i);

  /**
 * Retorna a quantidade total de entidades persistidas no repositório.
 *
 * @return o número total de entidades armazenadas
 */
  long count();

  /**
 * Remove a entidade identificada pelo valor fornecido.
 *
 * @param i identificador único da entidade a ser removida
 */
  void delete(I i);

  /****
 * Remove todas as entidades presentes no iterável fornecido.
 *
 * @param iterable coleção de entidades a serem removidas
 */
  void delete(Iterable<? extends T> iterable);

  /**
 * Remove todas as entidades persistidas do repositório.
 */
  void deleteAll();
}
