package br.edu.utfpr.pb.ext.server.generics;

import java.io.Serializable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class CrudServiceImpl<T, I extends Serializable> implements ICrudService<T, I> {

  /**
   * Retorna a instância de {@link JpaRepository} responsável pelas operações de persistência para a
   * entidade.
   *
   * @return o repositório JPA associado à entidade
   */
  protected abstract JpaRepository<T, I> getRepository();

  /**
   * Retorna uma lista com todas as entidades do tipo T.
   *
   * @return lista de todas as entidades encontradas
   */
  @Override
  public List<T> findAll() {
    return getRepository().findAll();
  }

  /**
   * Retorna uma lista de todas as entidades ordenadas conforme o parâmetro especificado.
   *
   * @param sort critério de ordenação a ser aplicado na consulta
   * @return lista de entidades ordenadas
   */
  @Override
  public List<T> findAll(Sort sort) {
    return getRepository().findAll(sort);
  }

  /**
   * Retorna uma página de entidades com base nas informações de paginação fornecidas.
   *
   * @param pageable informações de paginação e ordenação
   * @return uma página contendo as entidades encontradas
   */
  @Override
  public Page<T> findAll(Pageable pageable) {
    return getRepository().findAll(pageable);
  }

  /**
   * Salva uma entidade após aplicar os ganchos de pré e pós-processamento.
   *
   * <p>Executa o método {@code preSave} antes de persistir a entidade e {@code postsave} após a persistência.
   * Lança uma exceção se a entidade fornecida for nula.
   *
   * @param entity entidade a ser salva
   * @return a entidade salva, possivelmente modificada pelos ganchos de pré ou pós-processamento
   * @throws IllegalArgumentException se a entidade fornecida for nula
   */
  @Override
  public T save(T entity) {
    if (entity == null) {
      throw new IllegalArgumentException("O conteúdo a ser salvo não pode ser vazio.");
    }
    entity = preSave(entity);
    entity = getRepository().save(entity);
    entity = postsave(entity);
    return entity;
  }

  /**
   * Ponto de extensão chamado antes de salvar a entidade.
   *
   * <p>Pode ser sobrescrito para realizar validações ou modificações na entidade antes da
   * persistência.
   *
   * @param entity entidade a ser salva
   * @return a entidade possivelmente modificada antes do salvamento
   */
  public T preSave(T entity) {
    return entity;
  }

  /**
   * Ponto de extensão chamado após a persistência de uma entidade.
   *
   * <p>Pode ser sobrescrito para executar lógica adicional após o salvamento. Por padrão, retorna a
   * entidade sem modificações.
   *
   * @param entity entidade recém-persistida
   * @return a entidade, possivelmente modificada após o salvamento
   */
  public T postsave(T entity) {
    return entity;
  }

  /**
   * Salva a entidade e força a sincronização imediata das alterações com o banco de dados.
   *
   * @param entity entidade a ser salva
   * @return a entidade persistida após o flush
   */
  @Override
  public T saveAndFlush(T entity) {
    return getRepository().saveAndFlush(entity);
  }

  /**
   * Salva múltiplas entidades fornecidas por um Iterable.
   *
   * @param iterable coleção de entidades a serem salvas
   * @return as entidades salvas
   */
  @Override
  public Iterable<T> save(Iterable<T> iterable) {
    return getRepository().saveAll(iterable);
  }

  /**
   * Força a sincronização imediata das alterações pendentes no repositório com o banco de dados.
   */
  @Override
  public void flush() {
    getRepository().flush();
  }

  /**
   * Recupera uma entidade pelo seu identificador.
   *
   * @param i identificador da entidade a ser buscada
   * @return a entidade correspondente ao ID informado, ou {@code null} se não encontrada
   */
  @Override
  public T findOne(I i) {
    return getRepository().findById(i).orElse(null);
  }

  /**
   * Verifica se existe uma entidade com o identificador fornecido.
   *
   * @param i identificador da entidade a ser verificada
   * @return {@code true} se uma entidade com o ID especificado existir, caso contrário {@code
   *     false}
   */
  @Override
  public boolean exists(I i) {
    return getRepository().existsById(i);
  }

  /**
   * Retorna o número total de entidades persistidas.
   *
   * @return a quantidade total de entidades no repositório
   */
  @Override
  @Transactional(readOnly = true)
  public long count() {
    return getRepository().count();
  }

  /**
   * Remove a entidade correspondente ao identificador fornecido.
   *
   * @param i identificador da entidade a ser removida
   */
  @Override
  public void delete(I i) {
    getRepository().deleteById(i);
  }

  /**
   * Remove todas as entidades fornecidas do repositório.
   *
   * @param iterable coleção de entidades a serem removidas
   */
  @Override
  public void delete(Iterable<? extends T> iterable) {
    getRepository().deleteAll(iterable);
  }

  /** Remove todas as entidades do repositório. */
  @Override
  public void deleteAll() {
    getRepository().deleteAll();
  }
}
