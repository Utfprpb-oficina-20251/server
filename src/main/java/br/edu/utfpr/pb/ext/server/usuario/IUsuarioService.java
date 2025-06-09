package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import java.util.List;

public interface IUsuarioService extends ICrudService<Usuario, Long> {
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
  * Retorna uma lista de servidores ativos
  */
  List<Usuario> findServidoresAtivos();
}
