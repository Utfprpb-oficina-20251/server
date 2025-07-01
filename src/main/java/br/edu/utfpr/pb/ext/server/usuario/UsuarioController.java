package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.auth.dto.RespostaLoginDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.UsuarioLoginDTO;
import br.edu.utfpr.pb.ext.server.auth.jwt.JwtService;
import br.edu.utfpr.pb.ext.server.departamento.Departamento;
import br.edu.utfpr.pb.ext.server.departamento.DepartamentoRepository;
import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import br.edu.utfpr.pb.ext.server.usuario.authority.Authority;
import br.edu.utfpr.pb.ext.server.usuario.authority.AuthorityRepository;
import br.edu.utfpr.pb.ext.server.usuario.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "UsuarioController", description = "Endpoints for managing users")
public class UsuarioController extends CrudController<Usuario, UsuarioServidorResponseDTO, Long> {
  private final IUsuarioService usuarioService;
  private final ModelMapper modelMapper;
  private final JwtService jwtService;
  private final AuthorityRepository authorityRepository;
  private final UsuarioRepository usuarioRepository;
  private final DepartamentoRepository departamentoRepository;

  /**
   * Cria uma instância do controlador de usuários, inicializando os serviços necessários para
   * operações CRUD, mapeamento de entidades, geração de tokens JWT e gerenciamento de autoridades.
   *
   * @param usuarioService serviço responsável pelas operações de usuário
   * @param modelMapper instância para mapeamento entre entidades e DTOs
   * @param jwtService serviço para geração e manipulação de tokens JWT
   * @param authorityRepository repositório para consulta de autoridades (roles)
   */
  public UsuarioController(
      IUsuarioService usuarioService,
      ModelMapper modelMapper,
      JwtService jwtService,
      AuthorityRepository authorityRepository,
      UsuarioRepository usuarioRepository,
      DepartamentoRepository departamentoRepository) {
    super(Usuario.class, UsuarioServidorResponseDTO.class);
    this.usuarioService = usuarioService;
    this.modelMapper = modelMapper;
    this.jwtService = jwtService;
    this.authorityRepository = authorityRepository;
    this.usuarioRepository = usuarioRepository;
    this.departamentoRepository = departamentoRepository;
  }

  /**
   * Fornece o serviço CRUD específico para a entidade Usuario.
   *
   * @return a instância de ICrudService utilizada para operações com Usuario
   */
  @Override
  protected ICrudService<Usuario, Long> getService() {
    return usuarioService;
  }

  /**
   * Fornece a instância de ModelMapper utilizada para conversão entre entidades e DTOs neste
   * controlador.
   *
   * @return a instância de ModelMapper usada para mapeamento de objetos
   */
  @Override
  protected ModelMapper getModelMapper() {
    return modelMapper;
  }

  /**
   * Cria um novo usuário com perfil de servidor, atribui a autoridade "ROLE_SERVIDOR" e retorna um
   * token JWT com data de expiração.
   *
   * <p>Retorna HTTP 400 se a autoridade "ROLE_SERVIDOR" não for encontrada.
   *
   * @param usuarioServidorRequestDTO dados para criação do usuário com perfil de servidor
   * @return resposta HTTP 200 com token JWT e data de expiração em caso de sucesso, ou HTTP 400 se
   *     a autoridade estiver ausente
   */
  @Operation(
      summary = "Create a new servidor user",
      description = "Creates a new servidor user and returns a login response with a token.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RespostaLoginDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or missing authority",
            content = @Content(mediaType = "application/json"))
      })
  @PostMapping("/servidor")
  public ResponseEntity<RespostaLoginDTO> createServidor(
      @Valid @RequestBody UsuarioServidorRequestDTO usuarioServidorRequestDTO) {
    Usuario usuario = modelMapper.map(usuarioServidorRequestDTO, Usuario.class);
    Set<Authority> authorities = new HashSet<>();
    Authority servidorAuthority = authorityRepository.findByAuthority("ROLE_SERVIDOR").orElse(null);
    return getRespostaLoginDTOResponseEntity(usuario, authorities, servidorAuthority);
  }

  /****
   * Cria um novo usuário com perfil de aluno e retorna uma resposta de login com token JWT e tempo de expiração.
   * Retorna HTTP 200 com o token de autenticação e tempo de expiração caso a criação seja bem-sucedida.
   * Retorna HTTP 400 se a autoridade "ROLE_ALUNO" não for encontrada.
   * @param usuarioAlunoRequestDTO dados do usuário aluno a ser criado
   * @return resposta HTTP 200 com token e expiração, ou HTTP 400 se a autoridade não existir
   */
  @Operation(
      summary = "Create a new aluno user",
      description = "Creates a new aluno user and returns a login response with a token.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RespostaLoginDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or missing authority",
            content = @Content(mediaType = "application/json"))
      })
  @PostMapping("/aluno")
  public ResponseEntity<RespostaLoginDTO> createAluno(
      @Valid @RequestBody UsuarioAlunoRequestDTO usuarioAlunoRequestDTO) {
    Usuario usuario = modelMapper.map(usuarioAlunoRequestDTO, Usuario.class);
    Set<Authority> authorities = new HashSet<>();
    Authority alunoAuthority = authorityRepository.findByAuthority("ROLE_ALUNO").orElse(null);
    return getRespostaLoginDTOResponseEntity(usuario, authorities, alunoAuthority);
  }

  @GetMapping("/meu-perfil")
  public ResponseEntity<UsuarioLogadoInfoDTO> getMeuPerfil() {
    Usuario usuario = usuarioService.obterUsuarioLogado();
    UsuarioLogadoInfoDTO responseDTO = modelMapper.map(usuario, UsuarioLogadoInfoDTO.class);

    if (usuario.getDepartamento() != null) {
      responseDTO.setDepartamentoId(usuario.getDepartamento().getId());
    }

    return ResponseEntity.ok(responseDTO);
  }

  @Operation(
      summary = "Get all users",
      description = "Returns a list of all registered users in the system")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UsuarioProjetoDTO.class)))
      })
  @GetMapping("/executores")
  public ResponseEntity<List<UsuarioProjetoDTO>> getAllUsers() {
    List<Usuario> usuarios = usuarioService.findAll();
    List<UsuarioProjetoDTO> responseList =
        usuarios.stream()
            .map(usuario -> modelMapper.map(usuario, UsuarioProjetoDTO.class))
            .toList();
    return ResponseEntity.ok(responseList);
  }

  @Operation(
      summary = "Get all professors",
      description = "Returns a list of all users with emails ending with @utfpr.edu.br")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professors retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UsuarioProjetoDTO.class)))
      })
  @GetMapping("/professores")
  public ResponseEntity<List<UsuarioProjetoDTO>> getAllProfessors() {
    List<Usuario> usuarios = usuarioRepository.findAllByEmailEndingWith("@utfpr.edu.br");
    List<UsuarioProjetoDTO> responseList =
        usuarios.stream()
            .map(usuario -> modelMapper.map(usuario, UsuarioProjetoDTO.class))
            .toList();
    return ResponseEntity.ok(responseList);
  }

  @PutMapping("/meu-perfil")
  public ResponseEntity<UsuarioLogadoInfoDTO> updateProfile(
      @Valid @RequestBody UsuarioLogadoInfoDTO usuarioDTO) {

    Usuario currentUser = usuarioService.obterUsuarioLogado();

    currentUser.setNome(usuarioDTO.getNome());

    if (usuarioDTO.getCurso() != null) {
      currentUser.setCurso(usuarioDTO.getCurso());
    }

    if (usuarioDTO.getDepartamentoId() != null) {
      Departamento departamento =
          departamentoRepository
              .findById(usuarioDTO.getDepartamentoId())
              .orElseThrow(() -> new IllegalArgumentException("Departamento não encontrado"));
      currentUser.setDepartamento(departamento);
    }
    // Se departamentoId vier null, não altera o departamento atual.

    Usuario updatedUser = usuarioService.save(currentUser);

    UsuarioLogadoInfoDTO responseDTO = modelMapper.map(updatedUser, UsuarioLogadoInfoDTO.class);

    if (updatedUser.getDepartamento() != null) {
      responseDTO.setDepartamentoId(updatedUser.getDepartamento().getId());
    }

    return ResponseEntity.ok(responseDTO);
  }

  @GetMapping("/buscar-email/{email}")
  public ResponseEntity<UsuarioProjetoDTO> buscarPorEmail(@PathVariable String email) {
    Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
    if (usuario == null) {
      return ResponseEntity.notFound().build();
    }
    UsuarioProjetoDTO responseDTO = modelMapper.map(usuario, UsuarioProjetoDTO.class);
    return ResponseEntity.ok(responseDTO);
  }

  /**
   * Retorna uma resposta HTTP contendo um token JWT e o tempo de expiração após salvar o usuário
   * com a autoridade informada.
   *
   * <p>Retorna HTTP 400 se a autoridade for nula; caso contrário, adiciona a autoridade ao usuário,
   * salva-o, gera o token e retorna HTTP 200 com os dados de autenticação e identificação do
   * usuário.
   *
   * @param usuario entidade do usuário a ser salva
   * @param authorities conjunto de autoridades a serem atribuídas ao usuário
   * @param authority autoridade específica a ser adicionada ao usuário
   * @return resposta HTTP 200 com DTO contendo token JWT, expiração e dados do usuário, ou HTTP 400
   *     se a autoridade for nula
   */
  @NotNull private ResponseEntity<RespostaLoginDTO> getRespostaLoginDTOResponseEntity(
      Usuario usuario, Set<Authority> authorities, Authority authority) {
    if (authority == null) {
      return ResponseEntity.badRequest().body(null);
    }
    authorities.add(authority);
    usuario.setAuthorities(authorities);
    Usuario salvo = usuarioService.save(usuario);
    String token = jwtService.generateToken(salvo);
    long expiration = jwtService.getExpirationTime();
    return ResponseEntity.ok(
        new RespostaLoginDTO(
            token,
            expiration,
            new UsuarioLoginDTO(salvo.getEmail(), salvo.getNome(), salvo.getAuthoritiesStrings())));
  }
}
