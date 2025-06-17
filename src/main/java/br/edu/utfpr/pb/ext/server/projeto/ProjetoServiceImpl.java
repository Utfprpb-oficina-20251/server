package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjetoServiceImpl extends CrudServiceImpl<Projeto, Long> implements IProjetoService {
  private final ProjetoRepository projetoRepository;
  private final ModelMapper modelMapper;
  private final UsuarioRepository usuarioRepository;

  /**
   * Cria uma nova instância do serviço de projetos com o repositório fornecido.
   *
   * @param projetoRepository repositório utilizado para operações de persistência de projetos
   * @param modelMapper mapper para conversão entre objetos
   * @param usuarioRepository repositório para operações com usuários
   */
  public ProjetoServiceImpl(
      ProjetoRepository projetoRepository,
      ModelMapper modelMapper,
      UsuarioRepository usuarioRepository) {
    this.projetoRepository = projetoRepository;
    this.modelMapper = modelMapper;
    this.usuarioRepository = usuarioRepository;
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

  @Override
  public Projeto preSave(Projeto entity) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      String email = authentication.getName();

      if (entity.getResponsavel() == null) {
        Usuario usuarioAutenticado =
            usuarioRepository
                .findByEmail(email)
                .orElseThrow(
                    () -> new EntityNotFoundException("Usuário autenticado não encontrado"));
        entity.setResponsavel(usuarioAutenticado);
      }
    }
    return super.preSave(entity);
  }

  /**
   * Cancela um projeto existente, alterando seu status para {@code CANCELADO} e registrando uma
   * justificativa.
   *
   * <p>Apenas servidores (usuários com SIAPE preenchido) que fazem parte da equipe executora podem
   * realizar essa operação.
   *
   * @param id o ID do projeto a ser cancelado
   * @param dto objeto contendo a justificativa do cancelamento
   * @param usuarioId o ID do usuário que está tentando cancelar o projeto
   * @throws ResponseStatusException se: - o projeto não for encontrado (404), - a justificativa
   *     estiver vazia ou nula (400), - o projeto já estiver cancelado (400), - não existir equipe
   *     executora definida (400), - o usuário não for um servidor da equipe executora (403)
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

    if (projeto.getStatus() == StatusProjeto.CANCELADO) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Projeto já está cancelado");
    }

    if (projeto.getEquipeExecutora() == null || projeto.getEquipeExecutora().isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Projeto não possui equipe executora definida");
    }

    boolean isServidorNaEquipe =
        projeto.getEquipeExecutora().stream()
            .anyMatch(usuario -> usuario.getId().equals(usuarioId) && usuario.getSiape() != null);

    if (!isServidorNaEquipe) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Apenas servidores da equipe executora podem cancelar o projeto.");
    }

    projeto.setStatus(StatusProjeto.CANCELADO);
    projeto.setJustificativaCancelamento(dto.getJustificativa());

    projetoRepository.save(projeto);
  }

  @Override
  @Transactional
  public ProjetoDTO atualizarProjeto(Long id, ProjetoDTO dto) {
    Projeto projeto =
        this.projetoRepository
            .findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("Projeto com ID " + id + " não encontrado."));
    modelMapper.map(dto, projeto);
    Projeto projetoAtualizado = getRepository().save(projeto);

    return modelMapper.map(projetoAtualizado, ProjetoDTO.class);
  }
}
