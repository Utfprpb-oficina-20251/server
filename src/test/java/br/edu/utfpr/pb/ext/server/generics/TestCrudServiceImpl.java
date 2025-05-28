package br.edu.utfpr.pb.ext.server.generics;

import org.springframework.data.jpa.repository.JpaRepository;

/** Test implementation of CrudServiceImpl for unit testing. */
public class TestCrudServiceImpl extends CrudServiceImpl<TestEntity, Long> {

  private final JpaRepository<TestEntity, Long> repository;

  public TestCrudServiceImpl(JpaRepository<TestEntity, Long> repository) {
    this.repository = repository;
  }

  @Override
  protected JpaRepository<TestEntity, Long> getRepository() {
    return repository;
  }
}
