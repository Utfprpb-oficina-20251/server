package br.edu.utfpr.pb.ext.server.usuario.authority;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
  /**
   * Busca uma entidade Authority pelo valor do campo authority.
   *
   * @param authority valor do campo authority a ser pesquisado
   * @return um Optional contendo a entidade Authority correspondente, ou vazio se não encontrada
   */
  Optional<Authority> findByAuthority(String authority);
}
