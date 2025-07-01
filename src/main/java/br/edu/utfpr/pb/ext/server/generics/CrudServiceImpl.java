package br.edu.utfpr.pb.ext.server.generics;

import jakarta.persistence.EntityNotFoundException;
import java.io.Serializable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class CrudServiceImpl<T, I extends Serializable> implements ICrudService<T, I> {

  /**
   * Fornece o repositório JPA responsável pelas operações de persistência da entidade.
   *
   * @return o repositório JPA associado ao tipo de entidade gerenciado
   */
  protected abstract JpaRepository<T, I> getRepository();

  /**
   * Recupera todas as entidades do tipo T do repositório.
   *
   * @return lista contendo todas as entidades persistidas
   */
  @Override
  public List<T> findAll() {
    return getRepository().findAll();
  }

  /**
   * Recupera todas as entidades do repositório aplicando o critério de ordenação especificado.
   *
   * @param sort critério de ordenação a ser utilizado na consulta
   * @return lista de entidades ordenadas conforme o parâmetro fornecido
   */
  @Override
  public List<T> findAll(Sort sort) {
    return getRepository().findAll(sort);
  }

  /**
   * Recupera uma página de entidades do repositório conforme os critérios de paginação e ordenação
   * especificados.
   *
   * @param pageable objeto que define as informações de página e ordenação
   * @return página contendo as entidades correspondentes à consulta
   */
  @Override
  public Page<T> findAll(Pageable pageable) {
    return getRepository().findAll(pageable);
  }

  /**
   * Salva uma entidade, aplicando ganchos de pré e pós-processamento.
   *
   * <p>Executa o metodo {@code preSave} antes de persistir a entidade e {@code postsave} após a
   * persistência.
   *
   * @param entity entidade a ser salva
   * @return a entidade salva, possivelmente modificada pelos ganchos de pré ou pós-processamento
   * @throws IllegalArgumentException se a entidade fornecida for nula
   */
  @Override
  @Transactional
  public T save(T entity) {
    if (entity == null) {
      throw new IllegalArgumentException("O conteúdo a ser salvo não pode ser vazio.");
    }
    entity = preSave(entity);
    T savedEntity = getRepository().save(entity);
    savedEntity = postsave(savedEntity);
    return savedEntity;
  }

  /**
   * Permite customizar ou validar a entidade antes de sua persistência.
   *
   * <p>Pode ser sobrescrito para aplicar validações ou alterações na entidade antes de ser salva no
   * repositório.
   *
   * @param entity entidade a ser processada antes do salvamento
   * @return a entidade, possivelmente modificada, que será persistida
   */
  public T preSave(T entity) {
    return entity;
  }

  /**
   * Ponto de extensão chamado após a persistência de uma entidade.
   *
   * <p>Pode ser sobrescrito para executar lógica adicional após o salvamento da entidade. Por
   * padrão, retorna a entidade sem alterações.
   *
   * @param entity entidade recém-salva
   * @return a entidade, possivelmente modificada após o salvamento
   */
  public T postsave(T entity) {
    return entity;
  }

  /**
   * Salva a entidade e força a sincronização imediata das alterações com o banco de dados.
   *
   * @param entity entidade a ser salva e sincronizada
   * @return a entidade persistida após o flush
   */
  @Override
  @Transactional
  public T saveAndFlush(T entity) {
    return getRepository().saveAndFlush(entity);
  }

  /**
   * Salva em lote todas as entidades fornecidas.
   *
   * @param iterable coleção de entidades a serem salvas
   * @return Iterable contendo as entidades persistidas
   */
  @Override
  @Transactional
  public Iterable<T> save(Iterable<T> iterable) {
    return getRepository().saveAll(iterable);
  }

  /** Sincroniza imediatamente todas as alterações pendentes do repositório com o banco de dados. */
  @Override
  public void flush() {
    getRepository().flush();
  }

  /**
   * Busca uma entidade pelo identificador fornecido.
   *
   * @param i identificador da entidade
   * @return a entidade correspondente ao identificador
   * @throws EntityNotFoundException se nenhuma entidade for encontrada com o identificador
   *     informado
   */
  @Override
  public T findOne(I i) {
    return getRepository()
        .findById(i)
        .orElseThrow(() -> new EntityNotFoundException("Não há entidade com o id " + i));
  }

  /**
   * Verifica se existe uma entidade com o identificador especificado.
   *
   * @param i identificador da entidade
   * @return {@code true} se a entidade existir, {@code false} caso contrário
   */
  @Override
  public boolean exists(I i) {
    return getRepository().existsById(i);
  }

  /**
   * Retorna a quantidade total de entidades armazenadas no repositório.
   *
   * @return o número total de entidades persistidas
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
  @Transactional
  public void delete(I i) {
    getRepository().deleteById(i);
  }

  /**
   * Remove todas as entidades fornecidas da base de dados.
   *
   * @param iterable coleção de entidades a serem removidas
   */
  @Override
  @Transactional
  public void delete(Iterable<? extends T> iterable) {
    getRepository().deleteAll(iterable);
  }

  /**
   * Exclui permanentemente todas as entidades do repositório.
   *
   * <p>Esta operação remove todos os registros da entidade correspondente no banco de dados.
   */
  @Override
  @Transactional
  public void deleteAll() {
    getRepository().deleteAll();
  }
}
