package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SugestaoDeProjetoRepository extends JpaRepository<SugestaoDeProjeto, Long> {

  /**
 * Recupera todas as sugestões de projeto associadas a um aluno pelo seu ID.
 *
 * @param alunoId ID do aluno para o qual as sugestões de projeto serão buscadas
 * @return lista de sugestões de projeto vinculadas ao aluno informado
 */
  List<SugestaoDeProjeto> findByAlunoId(Long alunoId);

  /**
 * Retorna todas as sugestões de projeto associadas a um professor específico.
 *
 * @param professorId identificador único do professor
 * @return lista de sugestões de projeto vinculadas ao professor informado
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
