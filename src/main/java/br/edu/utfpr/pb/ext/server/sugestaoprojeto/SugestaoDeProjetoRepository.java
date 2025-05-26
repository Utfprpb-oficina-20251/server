package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SugestaoDeProjetoRepository extends JpaRepository<SugestaoDeProjeto, Long> {

  /**
 * Retorna todas as sugestões de projeto vinculadas a um aluno específico.
 *
 * @param alunoId identificador único do aluno
 * @return lista de sugestões de projeto associadas ao aluno
 */
  List<SugestaoDeProjeto> findByAlunoId(Long alunoId);

  /**
 * Busca todas as sugestões de projeto vinculadas a um professor específico.
 *
 * @param professorId identificador único do professor
 * @return lista de sugestões de projeto associadas ao professor
 */
  List<SugestaoDeProjeto> findByProfessorId(Long professorId);

  /**
 * Busca todas as sugestões de projeto com o status especificado.
 *
 * @param status status pelo qual as sugestões de projeto serão filtradas
 * @return lista de sugestões de projeto que possuem o status informado
 */
  List<SugestaoDeProjeto> findByStatus(StatusSugestao status);
}
