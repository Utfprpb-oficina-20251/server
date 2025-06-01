package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

/**
 * Implementação do serviço de Departamento.
 * Estende a classe genérica CrudServiceImpl para reaproveitamento de lógica CRUD.
 * Implementa a interface DepartamentoService para permitir futura expansão de regras de negócio.
 */
@Service
public class DepartamentoServiceImpl extends CrudServiceImpl<Departamento, Long>
        implements DepartamentoService {

  private final DepartamentoRepository repository;

  /**
   * Construtor que injeta o repositório específico de Departamento.
   *
   * @param repository Repositório JPA para a entidade Departamento.
   */
  public DepartamentoServiceImpl(DepartamentoRepository repository) {
    this.repository = repository;
  }

  /**
   * Retorna o repositório específico da entidade para uso na classe genérica.
   *
   * @return JpaRepository de Departamento.
   */
  @Override
  protected JpaRepository<Departamento, Long> getRepository() {
    return repository;
  }
}