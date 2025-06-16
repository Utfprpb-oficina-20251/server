package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

  @Override
  @Transactional
  public ProjetoDTO atualizarProjeto(Long id, ProjetoDTO dto) {

    Projeto projeto =
        this.projetoRepository
            .findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("Projeto com ID " + id + " não encontrado."));
    modelMapper.map(dto, projeto);
    Projeto projetoAtualizado = this.save(projeto);

    return modelMapper.map(projetoAtualizado, ProjetoDTO.class);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProjetoDTO> buscarProjetosPorFiltro(
      @NotNull FiltroProjetoDTO filtros /*, Pageable pageable */) {
    // 1. Chama o método privado para criar a Specification com base nos filtros
    Specification<Projeto> spec = criarSpecificationComFiltros(filtros);

    // 2. Executa a busca no repositório com a Specification
    List<Projeto> projetosEncontrados = projetoRepository.findAll(spec /*, pageable */);

    // 3. Mapeia a lista de entidades para uma lista de DTOs usando o ModelMapper
    return projetosEncontrados.stream()
            .map(projeto -> modelMapper.map(projeto, ProjetoDTO.class))
            .collect(Collectors.toList());
  }

  // --- NOVO MÉTODO PRIVADO PARA A LÓGICA DA CONSULTA ---
  /**
   * Cria um objeto Specification dinamicamente com base nos filtros fornecidos.
   * A lógica que estaria na classe ProjetoSpecification agora vive aqui.
   * @param filtros DTO com os critérios de busca.
   * @return Um objeto Specification<Projeto> pronto para ser usado na consulta.
   */
  private Specification<Projeto> criarSpecificationComFiltros(FiltroProjetoDTO filtros) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (filtros.titulo() != null && !filtros.titulo().isEmpty()) {
        predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("titulo")), "%" + filtros.titulo().toLowerCase() + "%"));
      }

      if (filtros.status() != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), filtros.status()));
      }

      if (filtros.dataInicioDe() != null) {
        predicates.add(
                criteriaBuilder.greaterThanOrEqualTo(root.get("dataInicio"), filtros.dataInicioDe().atStartOfDay())
        );      }
      if (filtros.dataInicioAte() != null) {
        predicates.add(criteriaBuilder.lessThan(root.get("dataInicio"), filtros.dataInicioAte().atStartOfDay()));
      }

      if (filtros.idResponsavel() != null) {
        predicates.add(criteriaBuilder.equal(root.join("responsavel").get("id"), filtros.idResponsavel()));
      }

      if (filtros.idMembroEquipe() != null) {
        predicates.add(criteriaBuilder.equal(root.join("equipeExecutora").get("id"), filtros.idMembroEquipe()));

      }

      if (filtros.idCurso() != null) {
        predicates.add(criteriaBuilder.equal(root.join("responsavel").join("curso").get("id"), filtros.idCurso()));
      }

      query.distinct(true);
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
