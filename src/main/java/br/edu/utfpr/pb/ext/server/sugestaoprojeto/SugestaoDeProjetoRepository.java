package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SugestaoDeProjetoRepository extends JpaRepository<SugestaoDeProjeto, Long> {

  List<SugestaoDeProjeto> findByAlunoId(Long alunoId);

  List<SugestaoDeProjeto> findByProfessorId(Long professorId);

  List<SugestaoDeProjeto> findByStatus(StatusSugestao status);
}
