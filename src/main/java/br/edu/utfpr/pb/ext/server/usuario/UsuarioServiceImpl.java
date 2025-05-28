package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl extends CrudServiceImpl<Usuario, Long> implements IUsuarioService {

  private final UsuarioRepository usuarioRepository;

  public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {

    this.usuarioRepository = usuarioRepository;
  }

  /**
   * Retorna o repositório JPA utilizado para operações CRUD da entidade Usuario.
   *
   * @return o repositório JpaRepository para Usuario
   */
  @Override
  protected JpaRepository<Usuario, Long> getRepository() {
    return usuarioRepository;
  }

  private static final String ROLE_SERVIDOR = "ROLE_SERVIDOR";

  /**
   * Obtém o usuário atualmente autenticado no contexto de segurança.
   *
   * @return o usuário autenticado
   * @throws IllegalStateException se não houver usuário autenticado ou se o principal não for uma instância de Usuario
   */
  public Usuario obterUsuarioLogado() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new IllegalStateException("Nenhum usuário autenticado!");
    }
    Object principal = auth.getPrincipal();
    if (!(principal instanceof Usuario)) {
      throw new IllegalStateException("Principal não é uma instância de Usuario!");
    }
    return (Usuario) principal;
  }

  /**
   * Valida se o usuário informado está ativo e possui o perfil de servidor.
   *
   * @param professor usuário a ser validado como professor
   * @throws IllegalArgumentException se o usuário estiver inativo ou não possuir o perfil de servidor
   */
  public void validarProfessor(Usuario professor) {
    if (!professor.isAtivo()) {
      throw new IllegalArgumentException("Professor deve estar ativo");
    }

    boolean temRoleServidor =
        professor.getAuthorities().stream().anyMatch(a -> ROLE_SERVIDOR.equals(a.getAuthority()));

    if (!temRoleServidor) {
      throw new IllegalArgumentException("Professor deve ter perfil de servidor");
    }
  }
}
