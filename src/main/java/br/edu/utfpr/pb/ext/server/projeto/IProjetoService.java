package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import jakarta.validation.Valid;

public interface IProjetoService extends ICrudService<Projeto, Long> {

  void cancelar(Long id, CancelamentoProjetoDTO dto, Long usuarioId);

  ProjetoDTO atualizarProjeto(Long id, @Valid ProjetoDTO dto);
}
