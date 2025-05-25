package br.edu.utfpr.pb.ext.server.usuario;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

  @Query("SELECT u FROM Usuario u WHERE u.ativo = true AND 'ROLE_SERVIDOR' MEMBER OF u.roles")
  List<Usuario> findServidoresAtivos();
}
