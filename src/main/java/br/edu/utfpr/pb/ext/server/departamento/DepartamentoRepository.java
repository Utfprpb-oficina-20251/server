package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.departamento.enums.Departamentos;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório JPA responsável por realizar operações de acesso a dados para a entidade {@link
 * Departamento}.
 */
public interface DepartamentoRepository
    extends JpaRepository<Departamento, Long> {

  /**
   * Busca uma associação pelo departamento informado.
   *
   * @param departamento Enum que representa o departamento
   * @return Optional contendo a associação, caso exista
   */
  Optional<Departamento> findByDepartamento(Departamentos departamento);
}
