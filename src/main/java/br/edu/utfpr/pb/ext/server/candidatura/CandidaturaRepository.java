package br.edu.utfpr.pb.ext.server.candidatura;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidaturaRepository extends JpaRepository<Candidatura, Long> {
  Optional<Candidatura> findByProjetoIdAndAlunoId(Long projetoId, Long alunoId);

  Optional<List<Candidatura>> findAllByProjetoIdAndStatus(Long projetoId, StatusCandidatura status);

  Optional<List<Candidatura>> findAllByAlunoId(Long alunoId);
}
