package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SugestaoDeProjetoRepository extends JpaRepository<SugestaoDeProjeto, Long> {

  /**
 * Recupera uma lista de sugestões de projeto associadas ao aluno especificado.
 *
 * @param alunoId identificador do aluno
 * @return lista de sugestões de projeto vinculadas ao aluno informado
 */
List<SugestaoDeProjeto> findByAlunoId(Long alunoId);

  /**
 * Retorna uma lista de sugestões de projeto associadas ao professor especificado.
 *
 * @param professorId identificador do professor
 * @return lista de sugestões de projeto vinculadas ao professor informado
 */
List<SugestaoDeProjeto> findByProfessorId(Long professorId);

  /**
 * Retorna uma lista de sugestões de projeto filtradas pelo status especificado.
 *
 * @param status o status pelo qual as sugestões de projeto serão filtradas
 * @return lista de sugestões de projeto com o status informado
 */
List<SugestaoDeProjeto> findByStatus(StatusSugestao status);
}
