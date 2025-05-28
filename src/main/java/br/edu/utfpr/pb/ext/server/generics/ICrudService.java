package br.edu.utfpr.pb.ext.server.generics;

import java.io.Serializable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface ICrudService<T, I extends Serializable> {

  /**
   * Recupera todas as entidades do tipo T.
   *
   * @return uma lista contendo todas as entidades encontradas
   */
  List<T> findAll();

  /**
   * Retorna todas as entidades ordenadas conforme o critério especificado.
   *
   * @param sort critério de ordenação a ser aplicado na consulta
   * @return lista de entidades ordenadas
   */
  List<T> findAll(Sort sort);

  /**
   * Retorna uma página de entidades de acordo com as informações de paginação fornecidas.
   *
   * @param pageable informações de paginação e ordenação
   * @return uma página contendo as entidades correspondentes
   */
  Page<T> findAll(Pageable pageable);

  /**
   * Salva a entidade fornecida e retorna a instância persistida.
   *
   * @param entity entidade a ser salva
   * @return a entidade salva, possivelmente com campos atualizados após a persistência
   */
  T save(T entity);

  /**
   * Salva a entidade fornecida e força a gravação imediata das alterações no banco de dados.
   *
   * @param entity entidade a ser salva
   * @return a instância da entidade salva
   */
  T saveAndFlush(T entity);

  /**
   * Salva múltiplas entidades e retorna as instâncias persistidas.
   *
   * @param iterable coleção de entidades a serem salvas
   * @return as entidades salvas
   */
  Iterable<T> save(Iterable<T> iterable);

  /**
   * Garante que todas as alterações pendentes nas entidades sejam imediatamente sincronizadas com o
   * banco de dados.
   */
  void flush();

  /**
   * Recupera uma entidade pelo seu identificador exclusivo.
   *
   * @param i identificador da entidade a ser buscada
   * @return a entidade correspondente ao identificador, ou {@code null} se não encontrada
   */
  T findOne(I i);

  /**
   * Verifica se existe uma entidade com o identificador fornecido.
   *
   * @param i identificador da entidade a ser verificada
   * @return true se a entidade existir, false caso contrário
   */
  boolean exists(I i);

  /**
   * Retorna o número total de entidades persistidas.
   *
   * @return a quantidade total de entidades
   */
  long count();

  /**
   * Remove a entidade correspondente ao identificador fornecido.
   *
   * @param i identificador da entidade a ser removida
   */
  void delete(I i);

  /****
   * Remove todas as entidades fornecidas pelo iterável.
   *
   * @param iterable coleção de entidades a serem removidas
   */
  void delete(Iterable<? extends T> iterable);

  /** Remove todas as entidades do repositório. */
  void deleteAll();
}
