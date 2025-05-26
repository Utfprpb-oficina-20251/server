package br.edu.utfpr.pb.ext.server.sugestaoprojeto.service;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.*;
import br.edu.utfpr.pb.ext.server.usuario.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SugestaoDeProjetoServiceImpl extends CrudServiceImpl<SugestaoDeProjeto, Long>
    implements ISugestaoDeProjetoService {

  private final SugestaoDeProjetoRepository repository;
  private final UsuarioRepository usuarioRepository;
  private final IUsuarioService usuarioService;

  /**
   * Retorna o repositório utilizado para operações CRUD de SugestaoDeProjeto.
   *
   * @return o repositório de SugestaoDeProjeto
   */
  @Override
  protected JpaRepository<SugestaoDeProjeto, Long> getRepository() {
    return repository;
  }

  /**
   * Prepara uma entidade SugestaoDeProjeto antes de ser salva.
   *
   * Define o usuário logado como aluno, inicializa o status como AGUARDANDO e, se informado, associa e valida o professor responsável. Lança EntityNotFoundException caso o professor especificado não seja encontrado.
   *
   * @param entity sugestão de projeto a ser preparada para persistência
   * @return a entidade SugestaoDeProjeto pronta para ser salva
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
    return super.preSave(entity);
  }

  /**
   * Retorna uma lista de sugestões de projeto associadas ao aluno especificado.
   *
   * O acesso é permitido apenas para usuários com o papel "ROLE_SERVIDOR" ou para o próprio aluno.
   *
   * @param alunoId ID do aluno cujas sugestões de projeto serão listadas
   * @return lista de sugestões de projeto do aluno informado
   */
  @PreAuthorize("hasRole('ROLE_SERVIDOR') or #alunoId == authentication.principal.id")
  public List<SugestaoDeProjeto> listarPorAluno(Long alunoId) {
    return repository.findByAlunoId(alunoId);
  }

  /**
   * Retorna a lista de sugestões de projeto associadas ao usuário atualmente logado.
   *
   * @return lista de sugestões de projeto do usuário logado
   */
  public List<SugestaoDeProjeto> listarSugestoesDoUsuarioLogado() {
    Usuario usuario = usuarioService.obterUsuarioLogado();
    return repository.findByAlunoId(usuario.getId());
  }
}
