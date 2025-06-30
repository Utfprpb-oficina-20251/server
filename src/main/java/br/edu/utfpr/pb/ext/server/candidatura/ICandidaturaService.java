package br.edu.utfpr.pb.ext.server.candidatura;

import java.util.List;

public interface ICandidaturaService {
  Candidatura candidatar(Long projetoId);

  void atualizarStatusCandidaturas(List<Candidatura> candidaturas);

  List<Candidatura> findAllByAlunoId(Long alunoId);

  List<Candidatura> findAllPendentesByProjetoId(Long projetoId);

  Candidatura findById(Long id);
}
