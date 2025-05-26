package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.generics.ICrudService;

public interface IUsuarioService extends ICrudService<Usuario, Long> {
  /**
 * Obtém o usuário atualmente autenticado no sistema.
 *
 * @return o usuário autenticado
 */
  Usuario obterUsuarioLogado();

  /**
 * Verifica se o usuário informado possui os requisitos necessários para ser reconhecido como professor no sistema.
 *
 * @param professor usuário a ser avaliado quanto ao status de professor
 */
  void validarProfessor(Usuario professor);
}
