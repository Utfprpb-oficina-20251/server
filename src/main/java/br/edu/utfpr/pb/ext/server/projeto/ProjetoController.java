package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioProjetoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("projeto")
@Tag(name = "Projeto", description = "Endpoints responsáveis por manipulação de projeto")
public class ProjetoController extends CrudController<Projeto, ProjetoDTO, Long> {

  private final IProjetoService projetoService;
  private final UsuarioRepository usuarioRepository;
  private final ModelMapper modelMapper;

  /**
   * Cria uma instância do controlador REST para gerenciar entidades de Projeto.
   *
   * @param projetoService serviço responsável pela lógica de negócios de projetos
   * @param modelMapper utilitário para conversão entre entidades e DTOs
   * @param usuarioRepository repositório para acesso a dados de usuários
   */
  public ProjetoController(
      IProjetoService projetoService,
      ModelMapper modelMapper,
      UsuarioRepository usuarioRepository) {
    super(Projeto.class, ProjetoDTO.class);
    this.projetoService = projetoService;
    this.usuarioRepository = usuarioRepository;
    this.modelMapper = modelMapper;
  }

  /**
   * Retorna a instância do serviço responsável pelas operações de CRUD de projetos.
   *
   * @return serviço de CRUD para entidades Projeto
   */
  @Override
  protected ICrudService<Projeto, Long> getService() {
    return this.projetoService;
  }

  /**
   * Retorna a instância de ModelMapper utilizada para conversão entre entidades e DTOs.
   *
   * @return a instância de ModelMapper
   */
  @Override
  protected ModelMapper getModelMapper() {
    return this.modelMapper;
  }

  /**
   * Cria um novo projeto a partir dos dados fornecidos e retorna os detalhes do projeto criado.
   *
   * <p>Valida se a equipe executora contém e-mails institucionais válidos e existentes no sistema.
   * Retorna HTTP 406 caso a lista de e-mails esteja vazia ou algum e-mail não corresponda a um
   * usuário cadastrado.
   *
   * @param dto Dados do projeto a ser criado, incluindo informações da equipe executora.
   * @return ResponseEntity contendo o ProjetoDTO criado e status HTTP 201 em caso de sucesso, ou
   *     status HTTP 406 em caso de erro de validação.
   */
  @Override
  @Operation(
      summary = "Create a new project",
      description = "Creates a new project and returns the created project details.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Project created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProjetoDTO.class))),
        @ApiResponse(
            responseCode = "406",
            description = "Invalid request, such as empty or invalid emails",
            content = @Content(mediaType = "application/json"))
      })
  @PostMapping
  public ResponseEntity<ProjetoDTO> create(@Valid @RequestBody ProjetoDTO dto) {
    Projeto projeto = new Projeto();
    List<String> emails =
        dto.getEquipeExecutora().stream().map(UsuarioProjetoDTO::getEmail).toList();
    if (emails.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "A equipe executora não pode estar vazia.");
    }
    ArrayList<Optional<Usuario>> usuarios = new ArrayList<>();
    for (String email : emails) {
      Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
      if (usuario.isEmpty()) {
        throw new ResponseStatusException(
            HttpStatus.NOT_ACCEPTABLE, "Usuário com e-mail " + email + " não encontrado.");
      }
      usuarios.add(usuario);
    }
    projeto.setTitulo(dto.getTitulo());
    projeto.setDescricao(dto.getDescricao());
    projeto.setJustificativa(dto.getJustificativa());
    projeto.setDataInicio(dto.getDataInicio());
    projeto.setDataFim(dto.getDataFim());
    projeto.setPublicoAlvo(dto.getPublicoAlvo());
    projeto.setVinculadoDisciplina(dto.isVinculadoDisciplina());
    projeto.setRestricaoPublico(dto.getRestricaoPublico());
    projeto.setEquipeExecutora(usuarios.stream().flatMap(Optional::stream).toList());
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
    Projeto projetoResponse = projetoService.save(projeto);
    ProjetoDTO projetoDTO = modelMapper.map(projetoResponse, ProjetoDTO.class);
    return ResponseEntity.status(HttpStatus.CREATED).body(projetoDTO);
  }

  @PreAuthorize("hasRole('SERVIDOR')")
  @PatchMapping("/{id}/cancelar")
  public ResponseEntity<Void> cancelar(
      @PathVariable Long id,
      @Valid @RequestBody CancelamentoProjetoDTO dto,
      @AuthenticationPrincipal Usuario usuario) {
    projetoService.cancelar(id, dto, usuario.getId());
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}")
  @PreAuthorize("@securityService.podeEditarProjeto(#id)")
  @Operation(summary = "Atualiza um projeto existente")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso"),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos ou violação de regra de negócio"),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado. Usuário não autorizado a editar este projeto"),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
      })
  @Override
  public ResponseEntity<ProjetoDTO> update(
      @PathVariable Long id, @Valid @RequestBody ProjetoDTO dto) {
    ProjetoDTO projetoAtualizado = projetoService.atualizarProjeto(id, dto);
    return ResponseEntity.ok(projetoAtualizado);
  }

  @Operation(
      summary = "Busca projetos por filtros",
      description =
          "Endpoint para buscar projetos de extensão com base em múltiplos critérios. Todos os parâmetros são opcionais.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Busca realizada com sucesso.",
        content = {
          @Content(
              mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = ProjetoDTO.class)))
        }),
    @ApiResponse(
        responseCode = "400",
        description = "Parâmetros inválidos fornecidos.",
        content = @Content)
  })
  @GetMapping("/buscar")
  public ResponseEntity<List<ProjetoDTO>> buscarProjetos(
      @Valid @ParameterObject FiltroProjetoDTO filtros) {
    List<ProjetoDTO> projetos = projetoService.buscarProjetosPorFiltro(filtros);
    return ResponseEntity.ok(projetos);
  }

  @Operation(
      summary = "Busca os projetos do usuário logado",
      description =
          "Retorna uma lista de todos os projetos em que o usuário autenticado faz parte da equipe executora. "
              + "A autenticação é necessária para acessar este recurso.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Busca realizada com sucesso. Retorna a lista de projetos do usuário, que pode estar vazia.",
            content = {
              @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = ProjetoDTO.class)))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso Negado. Este erro ocorre se o usuário não estiver autenticado.",
            content = @Content)
      })
  @GetMapping("/meusprojetos")
  public ResponseEntity<List<ProjetoDTO>> buscarMeusProjetos(
      @AuthenticationPrincipal Usuario userDetails) {
    FiltroProjetoDTO filtroCordenador =
        new FiltroProjetoDTO(null, null, null, null, userDetails.getId(), null, null);
    List<ProjetoDTO> projetos = projetoService.buscarProjetosPorFiltro(filtroCordenador);
    return ResponseEntity.ok(projetos);
  }
}
