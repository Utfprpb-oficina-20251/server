package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioProjetoDTO;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("projeto")
@Tag(name = "Projeto", description = "Endpoints responsáveis por manipulação de projeto")
public class ProjetoController extends CrudController<Projeto, ProjetoDTO, Long> {

  private final IProjetoService projetoService;
  private final UsuarioRepository usuarioRepository;
  private final ModelMapper modelMapper;

  public ProjetoController(
      IProjetoService projetoService,
      ModelMapper modelMapper,
      UsuarioRepository usuarioRepository) {
    super(Projeto.class, ProjetoDTO.class);
    this.projetoService = projetoService;
    this.usuarioRepository = usuarioRepository;
    this.modelMapper = modelMapper;
  }

  @Override
  protected ICrudService<Projeto, Long> getService() {
    return this.projetoService;
  }

  @Override
  protected ModelMapper getModelMapper() {
    return this.modelMapper;
  }

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
        dto.getEquipeExecutora().stream().map(UsuarioProjetoDTO::getEmailInstitucional).toList();
    if (emails.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
    }
    ArrayList<Optional<Usuario>> usuarios = new ArrayList<>();
    for (String email : emails) {
      Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
      if (usuario.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
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

  @Operation(
      summary = "Update an existing project",
      description = "Updates the details of an existing project by ID.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Project updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProjetoDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Project not found",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "406",
            description = "Invalid request, such as empty or invalid emails",
            content = @Content(mediaType = "application/json"))
      })

  /**
   * Atualiza um projeto existente com os dados informados.
   * @param id ID do projeto a ser atualizado.
   * @param projetoDTO dados atualizados do projeto.
   * @return Projeto atualizado.
   */

  @PutMapping("/{id}")
  public ResponseEntity<ProjetoDTO> update(
      @PathVariable Long id, @Valid @RequestBody ProjetoDTO dto) {

    Optional<Projeto> projetoOptional = projetoService.findById(id);

    if (projetoOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    Projeto projeto = projetoOptional.get();

    List<String> emails =
        dto.getEquipeExecutora().stream().map(UsuarioProjetoDTO::getEmailInstitucional).toList();

    if (emails.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
    }

    ArrayList<Optional<Usuario>> usuarios = new ArrayList<>();
    for (String email : emails) {
      Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
      if (usuario.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
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
    Projeto projetoResponse = projetoService.save(projeto);
    ProjetoDTO projetoDTO = modelMapper.map(projetoResponse, ProjetoDTO.class);

    return ResponseEntity.ok(projetoDTO);
  }

  @Operation(summary = "Get all projects", description = "Returns all projects from the database.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of projects",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProjetoDTO.class)))
      })
  @GetMapping
  public ResponseEntity<List<ProjetoDTO>> findAll() {
    List<Projeto> projetos = projetoService.findAll();
    List<ProjetoDTO> projetoDTOs =
        projetos.stream().map(projeto -> modelMapper.map(projeto, ProjetoDTO.class)).toList();
    return ResponseEntity.ok(projetoDTOs);
  }
}
