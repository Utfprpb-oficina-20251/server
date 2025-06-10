package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjetoServiceImpl extends CrudServiceImpl<Projeto, Long> implements IProjetoService {
  private final ProjetoRepository projetoRepository;

  /**
   * Cria uma nova instância do serviço de projetos com o repositório fornecido.
   *
   * @param projetoRepository repositório utilizado para operações de persistência de projetos
   */
  public ProjetoServiceImpl(ProjetoRepository projetoRepository) {
    this.projetoRepository = projetoRepository;
  }

  /**
   * Retorna o repositório JPA utilizado para operações CRUD com a entidade Projeto.
   *
   * @return o repositório ProjetoRepository associado à entidade Projeto
   */
  @Override
  protected JpaRepository<Projeto, Long> getRepository() {
    return projetoRepository;
  }

  /**
   * Cancela um projeto existente, alterando seu status para {@code CANCELADO} e registrando uma
   * justificativa.
   *
   * <p>Somente o responsável principal pelo projeto está autorizado a realizar essa operação.
   *
   * @param id o identificador do projeto a ser cancelado
   * @param dto objeto contendo a justificativa do cancelamento
   * @param usuarioId identificador do usuário que está tentando realizar o cancelamento
   * @throws ResponseStatusException se o projeto não for encontrado (404) ou se o usuário não for o
   *     responsável (403)
   */
  @Override
  public void cancelar(Long id, CancelamentoProjetoDTO dto, Long usuarioId) {
    if (dto.getJustificativa() == null || dto.getJustificativa().trim().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A justificativa é obrigatória.");
    }
    Projeto projeto =
        projetoRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));

    // Aqui assumimos o primeiro da equipe como responsável principal
    boolean isResponsavelPrincipal =
        projeto.getEquipeExecutora().stream()
            .findFirst()
            .map(u -> u.getId().equals(usuarioId))
            .orElse(false);

    if (!isResponsavelPrincipal) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Apenas o responsável principal pode cancelar o projeto.");
    }

    projeto.setStatus(StatusProjeto.CANCELADO);
    projeto.setJustificativaCancelamento(dto.getJustificativa());

    projetoRepository.save(projeto);
  }
}
