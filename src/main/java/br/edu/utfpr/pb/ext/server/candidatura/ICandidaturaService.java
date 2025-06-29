package br.edu.utfpr.pb.ext.server.candidatura;

public interface ICandidaturaService {
  CandidaturaDTO candidatar(Long projetoId, Long alunoId);
}
