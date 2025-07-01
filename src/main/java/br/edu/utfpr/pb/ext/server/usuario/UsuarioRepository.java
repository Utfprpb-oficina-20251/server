package br.edu.utfpr.pb.ext.server.usuario;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  /**
   * Retorna um usuário correspondente ao endereço de e-mail informado, caso exista.
   *
   * @param email endereço de e-mail a ser pesquisado
   * @return um Optional contendo o usuário encontrado ou vazio se não houver correspondência
   */
  Optional<Usuario> findByEmail(String email);

  /**
   * Busca um usuário pelo CPF informado.
   *
   * @param cpf número do Cadastro de Pessoa Física (CPF) do usuário
   * @return um Optional contendo o usuário correspondente, ou vazio se não encontrado
   */
  Optional<Usuario> findByCpf(String cpf);

  /**
   * Busca um usuário pelo número de SIAPE informado.
   *
   * @param siape número de identificação do servidor público (SIAPE)
   * @return um Optional contendo o usuário correspondente, caso exista
   */
  Optional<Usuario> findBySiape(String siape);

  /**
   * Busca um usuário pelo número de registro acadêmico (RA).
   *
   * @param ra número de registro acadêmico do usuário
   * @return um Optional contendo o usuário correspondente, caso exista
   */
  Optional<Usuario> findByRegistroAcademico(String ra);

  /**
   * Busca todos os usuários cujo e-mail termina com o domínio especificado.
   *
   * @param dominioEmail domínio do e-mail (por exemplo, "@utfpr.edu.br")
   * @return @return lista de usuários que atendem ao critério; lista vazia se nenhum encontrado
   */
  List<Usuario> findAllByEmailEndingWith(String dominioEmail);

  /**
   * Busca usuários cujo e-mail contém a substring informada.
   *
   * @param emailParcial parte do e-mail a ser pesquisada
   * @return lista de Optional contendo os usuários encontrados
   */
  List<Usuario> findByEmailContaining(String emailParcial);

  /**
   * Busca usuários cujo nome contém a substring informada (case insensitive).
   *
   * @param nomeParcial parte do nome a ser pesquisada
   * @return lista de Optional contendo os usuários encontrados
   */
  List<Usuario> findByNomeContainingIgnoreCase(String nomeParcial);
}
