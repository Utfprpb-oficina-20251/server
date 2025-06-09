package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.generics.ICrudService;

public interface DepartamentoService extends ICrudService<Departamento, Long> {
  void associarResponsavel(Long departamentoId, Long usuarioId);
}
