package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IUsuarioService extends ICrudService<Usuario, Long>, UserDetailsService {
  /**
   * Retorna o usuário atualmente autenticado no sistema.
   *
   * @return o usuário autenticado ou {@code null} se não houver usuário autenticado
   */
  Usuario obterUsuarioLogado();

  /**
   * Valida se o usuário atende aos critérios exigidos para ser considerado professor no sistema.
   *
   * @param professor usuário a ser validado como professor
   */
  void validarProfessor(Usuario professor);

  /**
   * Ativa um usuário identificado pelo e-mail.
   *
   * @param email e-mail do usuário a ser ativado
   */
  void ativarUsuario(String email);
}
