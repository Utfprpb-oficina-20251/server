package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SugestaoDeProjetoRepository extends JpaRepository<SugestaoDeProjeto, Long> {

  /**
 * Recupera todas as sugestões de projeto associadas a um determinado aluno pelo seu ID.
 *
 * @param alunoId ID do aluno para filtrar as sugestões de projeto
 * @return lista de sugestões de projeto vinculadas ao aluno especificado
 */
  List<SugestaoDeProjeto> findByAlunoId(Long alunoId);

  /**
 * Retorna todas as sugestões de projeto associadas ao professor informado.
 *
 * @param professorId identificador único do professor
 * @return lista de sugestões de projeto vinculadas ao professor especificado
 */
  List<SugestaoDeProjeto> findByProfessorId(Long professorId);

  /**
 * Retorna todas as sugestões de projeto que possuem o status especificado.
 *
 * @param status status pelo qual as sugestões de projeto serão filtradas
 * @return lista de sugestões de projeto com o status informado
 */
  List<SugestaoDeProjeto> findByStatus(StatusSugestao status);
}
