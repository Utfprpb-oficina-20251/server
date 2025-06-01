package br.edu.utfpr.pb.ext.server.generics;

import org.springframework.data.jpa.repository.JpaRepository;

/** Test implementation of CrudServiceImpl for unit testing. */
public class TestCrudServiceImpl extends CrudServiceImpl<TestEntity, Long> {

  private final JpaRepository<TestEntity, Long> repository;

  /**
   * Cria uma instância de TestCrudServiceImpl com o repositório fornecido.
   *
   * @param repository instância de JpaRepository para operações CRUD com TestEntity
   */
  public TestCrudServiceImpl(JpaRepository<TestEntity, Long> repository) {
    this.repository = repository;
  }

  /**
   * Retorna a instância do repositório associado à entidade TestEntity.
   *
   * @return o repositório JpaRepository utilizado para operações CRUD com TestEntity
   */
  @Override
  protected JpaRepository<TestEntity, Long> getRepository() {
    return repository;
  }
}
