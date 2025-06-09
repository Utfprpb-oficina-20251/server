package br.edu.utfpr.pb.ext.server.sugestaoprojeto.service;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import br.edu.utfpr.pb.ext.server.curso.CursoRepository;
import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.*;
import br.edu.utfpr.pb.ext.server.usuario.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SugestaoDeProjetoServiceImpl extends CrudServiceImpl<SugestaoDeProjeto, Long>
    implements ISugestaoDeProjetoService {

  private final SugestaoDeProjetoRepository repository;
  private final UsuarioRepository usuarioRepository;
  private final IUsuarioService usuarioService;
  private final CursoRepository cursoRepository;

  /**
   * Fornece o repositório específico para operações CRUD da entidade SugestaoDeProjeto.
   *
   * @return o repositório de SugestaoDeProjeto
   */
  @Override
  protected JpaRepository<SugestaoDeProjeto, Long> getRepository() {
    return repository;
  }

  /**
   * Prepara a entidade SugestaoDeProjeto para persistência, definindo o usuário logado como aluno,
   * status como AGUARDANDO e validando o professor, se informado.
   *
   * <p>Caso um professor seja especificado, valida sua existência e papel; lança
   * EntityNotFoundException se não encontrado.
   *
   * @param entity sugestão de projeto a ser preparada para salvamento
   * @return a entidade SugestaoDeProjeto pronta para persistência
   */
  @Override
  public SugestaoDeProjeto preSave(SugestaoDeProjeto entity) {

    Usuario aluno = usuarioService.obterUsuarioLogado();

    entity.setStatus(StatusSugestao.AGUARDANDO);
    entity.setAluno(aluno);

    if (entity.getProfessor() != null && entity.getProfessor().getId() != null) {
      Usuario professor =
          usuarioRepository
              .findById(entity.getProfessor().getId())
              .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado"));

      usuarioService.validarProfessor(professor);
      entity.setProfessor(professor);
    }

    if (entity.getCurso() != null && entity.getCurso().getId() != null) {
      Long cursoId = entity.getCurso().getId();
      Curso cursoGerenciado =
              cursoRepository
                      .findById(cursoId)
                      .orElseThrow(() -> new EntityNotFoundException("Curso não encontrado com ID: " + cursoId));
      entity.setCurso(cursoGerenciado); // Define a entidade Curso GERENCIADA
    }

    return entity;
  }

  @Override
  @Transactional // ESSENCIAL para operações de escrita no banco!
  public SugestaoDeProjeto save(SugestaoDeProjeto entity) {
    // A chamada super.save(entity) irá executar:
    // 1. this.preSave(entity) (o método preSave desta classe SugestaoDeProjetoServiceImpl)
    // 2. getRepository().save(entity)
    // 3. this.postsave(entity) (que é um no-op no CrudServiceImpl por padrão)
    return super.save(entity);
  }

  /**
   * Lista as sugestões de projeto vinculadas ao aluno identificado pelo ID fornecido.
   *
   * <p>O acesso é restrito a usuários com o papel "ROLE_SERVIDOR" ou ao próprio aluno.
   *
   * @param alunoId identificador do aluno cujas sugestões de projeto serão retornadas
   * @return lista de sugestões de projeto associadas ao aluno
   */
  @PreAuthorize("hasRole('ROLE_SERVIDOR') or #alunoId == authentication.principal.id")
  public List<SugestaoDeProjeto> listarPorAluno(Long alunoId) {
    return repository.findByAlunoId(alunoId);
  }

  /**
   * Recupera todas as sugestões de projeto vinculadas ao usuário atualmente autenticado.
   *
   * @return lista de sugestões de projeto associadas ao usuário logado
   */
  public List<SugestaoDeProjeto> listarSugestoesDoUsuarioLogado() {
    Usuario usuario = usuarioService.obterUsuarioLogado();
    return repository.findByAlunoId(usuario.getId());
  }
}
