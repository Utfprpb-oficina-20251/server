package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.generics.ICrudService;

public interface IProjetoService extends ICrudService<Projeto, Long> {
  void cancelar(Long id, CancelamentoProjetoDTO dto, Long usuarioId);
}
