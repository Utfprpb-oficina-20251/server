package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.generics.ICrudService;

public interface IUsuarioService extends ICrudService<Usuario, Long> {
  /**
   * Retorna o usuário atualmente autenticado no sistema.
   *
   * @return o usuário logado no momento
   */
  Usuario obterUsuarioLogado();

  /**
   * Valida se o usuário fornecido atende aos critérios necessários para ser considerado professor.
   *
   * @param professor usuário a ser validado como professor
   */
  void validarProfessor(Usuario professor);
}
