package br.edu.utfpr.pb.ext.server.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  /**
 * Busca um usuário pelo endereço de e-mail.
 *
 * @param email o endereço de e-mail do usuário a ser buscado
 * @return um Optional contendo o usuário correspondente, caso exista
 */
Optional<Usuario> findByEmail(String email);

  Optional<Usuario> findByCpf(String cpf);

  Optional<Usuario> findBySiape(String siape);
}
