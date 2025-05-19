package br.edu.utfpr.pb.ext.server.usuario.authority;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
  Optional<Authority> findByAuthority(String authority);
}
