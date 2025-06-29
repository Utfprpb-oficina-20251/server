package br.edu.utfpr.pb.ext.server.candidatura;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidaturaRepository extends JpaRepository<Candidatura, Long> {
  boolean existsByProjetoIdAndAlunoId(Long projetoId, Long alunoId);

  long countByProjetoId(Long projetoId);
}
