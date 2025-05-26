package br.edu.utfpr.pb.ext.server.usuario;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

  private static final String ROLE_SERVIDOR = "ROLE_SERVIDOR";

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
