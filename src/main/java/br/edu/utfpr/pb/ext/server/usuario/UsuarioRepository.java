package br.edu.utfpr.pb.ext.server.usuario;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
  Optional<Usuario> findByEmail(String email);
}
