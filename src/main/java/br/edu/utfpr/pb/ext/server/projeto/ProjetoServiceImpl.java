package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.event.EventPublisher;
import br.edu.utfpr.pb.ext.server.file.FileInfoDTO;
import br.edu.utfpr.pb.ext.server.file.FileService;
import br.edu.utfpr.pb.ext.server.file.img.ImageUtils;
import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class ProjetoServiceImpl extends CrudServiceImpl<Projeto, Long> implements IProjetoService {
  private final ProjetoRepository projetoRepository;
  private final ModelMapper modelMapper;
  private final UsuarioRepository usuarioRepository;
  private final FileService fileService;
  private final ImageUtils imageUtils;

  private final EventPublisher eventPublisher;

  /**
   * Constrói o serviço de projetos inicializando os repositórios, utilitários e serviços
   * necessários para operações de negócio relacionadas a projetos.
   *
   * @param projetoRepository repositório para persistência de projetos
   * @param modelMapper utilitário para conversão entre entidades e DTOs
   * @param usuarioRepository repositório para operações com usuários
   * @param fileService serviço para armazenamento de arquivos
   * @param imageUtils utilitário para validação e processamento de imagens
   */
  public ProjetoServiceImpl(
      ProjetoRepository projetoRepository,
      ModelMapper modelMapper,
      UsuarioRepository usuarioRepository,
      FileService fileService,
      ImageUtils imageUtils,
      EventPublisher eventPublisher) {
    this.projetoRepository = projetoRepository;
    this.modelMapper = modelMapper;
    this.usuarioRepository = usuarioRepository;
    this.fileService = fileService;
    this.imageUtils = imageUtils;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Retorna o repositório JPA responsável pelas operações CRUD da entidade Projeto.
   *
   * @return o ProjetoRepository utilizado para persistência de Projetos
   */
  @Override
  protected JpaRepository<Projeto, Long> getRepository() {
    return projetoRepository;
  }

  /**
   * Prepara a entidade Projeto antes de salvá-la, atribuindo status inicial, responsável, equipe
   * executora e processando a imagem do projeto.
   *
   * <p>Se o projeto for novo, define o status como EM_ANDAMENTO. Garante que o responsável e a
   * equipe executora estejam corretamente atribuídos e que a imagem, se fornecida em Base64, seja
   * validada, armazenada e tenha sua URL atualizada.
   *
   * @param projeto Entidade Projeto a ser preparada para persistência.
   * @return A entidade Projeto pronta para ser salva.
   */
  @Override
  public Projeto preSave(Projeto projeto) {

    if (projeto.getId() == null) {
      projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
    }

    atribuirResponsavel(projeto);
    atribuirUsuariosEquipeExecutora(projeto);
    processaImagemUrl(projeto);
    return super.preSave(projeto);
  }

  /**
   * Executa ações após salvar um projeto, enviando notificações apropriadas dependendo se o projeto
   * é novo ou atualizado.
   *
   * <p>Este metodo é chamado automaticamente após a persistência da entidade e determina se o
   * projeto foi recentemente criado (baseado na data de criação) ou se foi atualizado, publicando o
   * evento adequado para cada caso.
   *
   * @param entity O projeto que foi salvo
   * @return A entidade após o processamento de pós-salvamento
   */
  @Override
  public Projeto postsave(Projeto entity) {
    Projeto savedEntity = super.postsave(entity);

    boolean isNew = entity.getId() == null || entity.getId() <= 0L;

    if (isNew) {
      eventPublisher.publishProjetoCriado(savedEntity);
    } else {
      eventPublisher.publishProjetoAtualizado(savedEntity);
    }

    return savedEntity;
  }

  /**
   * Processa a URL da imagem do projeto, validando e decodificando uma imagem em Base64,
   * armazenando-a e atualizando a URL do projeto com o endereço do arquivo salvo.
   *
   * <p>Caso a imagem não seja válida ou ocorra erro no processamento, lança uma exceção HTTP 500.
   */
  private void processaImagemUrl(Projeto projeto) {
    String imagemUrl = projeto.getImagemUrl();
    if (imagemUrl == null || imagemUrl.isBlank()) {
      return;
    }

    ImageUtils.DecodedImage decodedImage = imageUtils.validateAndDecodeBase64Image(imagemUrl);

    if (decodedImage != null) {
      try {
        String filename =
            "projeto-imagem." + ImageUtils.getFileExtensionFromMimeType(decodedImage.contentType());
        FileInfoDTO fileInfo =
            fileService.store(decodedImage.data(), decodedImage.contentType(), filename);
        projeto.setImagemUrl(fileInfo.getUrl());
      } catch (Exception e) {
        log.error("Falha ao processar a imagem do projeto.", e);
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao processar a imagem do projeto.", e);
      }
    }
  }

  /**
   * Valida e substitui os usuários da equipe executora do projeto pelos respectivos registros
   * completos do banco de dados.
   *
   * <p>Para cada usuário informado na equipe executora, verifica se o objeto e o e-mail estão
   * presentes e válidos. Caso contrário, lança uma exceção HTTP 400. Se o usuário não for
   * encontrado pelo e-mail, lança exceção HTTP 406. Ao final, atualiza a equipe executora do
   * projeto com a lista de usuários carregados do banco.
   */
  private void atribuirUsuariosEquipeExecutora(Projeto entity) {
    List<Usuario> entidadesUsuarioCarregadas =
        entity.getEquipeExecutora().stream()
            .map(
                usuario -> {
                  if (usuario == null) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Usuário inválido na requisição");
                  }
                  if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Usuário com email não informado");
                  }

                  return usuarioRepository
                      .findByEmail(usuario.getEmail())
                      .orElseThrow(
                          () ->
                              new ResponseStatusException(
                                  HttpStatus.NOT_ACCEPTABLE,
                                  "Usuário com email " + usuario.getEmail() + " não encontrado."));
                })
            .toList();
    entity.setEquipeExecutora(entidadesUsuarioCarregadas);
  }

  /**
   * Define o usuário autenticado como responsável pelo projeto caso ainda não esteja definido.
   *
   * <p>Lança EntityNotFoundException se o usuário autenticado não for encontrado no banco de dados.
   */
  private void atribuirResponsavel(Projeto entity) {
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
    processaImagemUrl(projeto);
    Projeto projetoAtualizado = getRepository().save(projeto);

    return modelMapper.map(projetoAtualizado, ProjetoDTO.class);
  }

  /**
   * Busca projetos aplicando filtros dinâmicos e retorna a lista de resultados como DTOs.
   *
   * @param filtros critérios de filtragem para a busca dos projetos
   * @return lista de projetos encontrados convertidos para DTOs
   */
  @Override
  @Transactional(readOnly = true)
  public List<ProjetoDTO> buscarProjetosPorFiltro(
      @NotNull FiltroProjetoDTO filtros /*, Pageable pageable */) {
    Specification<Projeto> spec = criarSpecificationComFiltros(filtros);

    // 2. Executa a busca no repositório com a Specification
    List<Projeto> projetosEncontrados = projetoRepository.findAll(spec /*, pageable */);

    // 3. Mapeia a lista de entidades para uma lista de DTOs usando o ModelMapper
    return projetosEncontrados.stream()
        .map(projeto -> modelMapper.map(projeto, ProjetoDTO.class))
        .toList();
  }

  @Override
  public List<String> getAlunosExecutores(List<Long> idsProjeto) {
    return idsProjeto.stream()
        .map(projetoRepository::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(
            projeto ->
                projeto.getEquipeExecutora().stream()
                    .filter(this::isAluno)
                    .map(executor -> formatarAlunoExecutor(executor, projeto)))
        .toList();
  }

  private boolean isAluno(Usuario usuario) {
    return usuario.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ALUNO"));
  }

  private String formatarAlunoExecutor(Usuario executor, Projeto projeto) {
    return String.format(
        "%s - %s - %s", executor.getNome(), executor.getEmail(), projeto.getTitulo());
  }

  /**
   * Cria dinamicamente uma Specification para a entidade Projeto com base nos filtros informados.
   *
   * <p>Os filtros suportados incluem título (busca parcial, case-insensitive), status, intervalo de
   * datas de início, ID do responsável, ID de membro da equipe executora e ID do curso do
   * responsável. Garante que os resultados sejam distintos.
   *
   * @param filtros DTO contendo os critérios de filtragem dos projetos.
   * @return Specification<Projeto> configurada conforme os filtros fornecidos.
   */
  private Specification<Projeto> criarSpecificationComFiltros(FiltroProjetoDTO filtros) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (filtros.titulo() != null && !filtros.titulo().isEmpty()) {
        predicates.add(
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("titulo")),
                "%" + filtros.titulo().toLowerCase() + "%"));
      }

      if (filtros.status() != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), filtros.status()));
      }

      if (filtros.dataInicioDe() != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(
                root.get("dataInicio"), filtros.dataInicioDe().atStartOfDay()));
      }
      if (filtros.dataInicioAte() != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(
                root.get("dataInicio"), filtros.dataInicioAte().atTime(23, 59, 59)));
      }

      if (filtros.idResponsavel() != null) {
        predicates.add(
            criteriaBuilder.equal(root.join("responsavel").get("id"), filtros.idResponsavel()));
      }

      if (filtros.idMembroEquipe() != null) {
        predicates.add(
            criteriaBuilder.equal(
                root.join("equipeExecutora").get("id"), filtros.idMembroEquipe()));
      }

      if (filtros.idCurso() != null) {
        predicates.add(
            criteriaBuilder.equal(
                root.join("responsavel").join("curso").get("id"), filtros.idCurso()));
      }

      if (filtros.cargaHorariaMinima() != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(
                root.get("cargaHoraria"), filtros.cargaHorariaMinima()));
      }

      // NOVO FILTRO: Carga Horária Máxima
      if (filtros.cargaHorariaMaxima() != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(
                root.get("cargaHoraria"), filtros.cargaHorariaMaxima()));
      }

      // NOVO FILTRO: Nome do Curso (busca parcial)
      if (filtros.nomeCurso() != null && !filtros.nomeCurso().isEmpty()) {
        predicates.add(
            criteriaBuilder.like(
                criteriaBuilder.lower(root.join("responsavel").join("curso").get("nome")),
                "%" + filtros.nomeCurso().toLowerCase() + "%"));
      }

      if (filtros.temVagas() != null) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Projeto> subRoot = subquery.from(Projeto.class);
        Join<Projeto, Usuario> equipeJoin = subRoot.join("equipeExecutora");
        subquery.select(criteriaBuilder.count(equipeJoin));
        subquery.where(criteriaBuilder.equal(subRoot, root));

        Path<Long> qtdeVagasPath = root.get("qtdeVagas");

        if (filtros.temVagas()) {
          predicates.add(criteriaBuilder.greaterThan(qtdeVagasPath, subquery));
        } else {
          predicates.add(criteriaBuilder.lessThanOrEqualTo(qtdeVagasPath, subquery));
        }
      }

      query.distinct(true);
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
