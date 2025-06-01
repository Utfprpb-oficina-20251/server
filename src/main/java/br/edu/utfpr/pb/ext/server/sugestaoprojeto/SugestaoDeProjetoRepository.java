package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SugestaoDeProjetoRepository extends JpaRepository<SugestaoDeProjeto, Long> {

  /**
 * Retorna todas as sugestões de projeto vinculadas a um aluno específico pelo seu ID.
 *
 * @param alunoId identificador do aluno cujas sugestões de projeto serão recuperadas
 * @return lista de sugestões de projeto associadas ao aluno informado
 */
  List<SugestaoDeProjeto> findByAlunoId(Long alunoId);

  /**
 * Recupera todas as sugestões de projeto vinculadas a um professor específico.
 *
 * @param professorId identificador do professor
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
