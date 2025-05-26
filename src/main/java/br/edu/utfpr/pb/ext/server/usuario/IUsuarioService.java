package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.generics.ICrudService;

public interface IUsuarioService extends ICrudService<Usuario, Long> {
  Usuario obterUsuarioLogado();

  void validarProfessor(Usuario professor);
}
