package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.auth.dto.RespostaLoginDTO;
import br.edu.utfpr.pb.ext.server.auth.jwt.JwtService;
import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import br.edu.utfpr.pb.ext.server.usuario.authority.Authority;
import br.edu.utfpr.pb.ext.server.usuario.authority.AuthorityRepository;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioAlunoRequestDTO;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorRequestDTO;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "UsuarioController", description = "Endpoints for managing users")
public class UsuarioController extends CrudController<Usuario, UsuarioServidorResponseDTO, Long> {
  private final IUsuarioService usuarioService;
  private final ModelMapper modelMapper;
  private final JwtService jwtService;
  private final AuthorityRepository authorityRepository;

  public UsuarioController(
      IUsuarioService usuarioService,
      ModelMapper modelMapper,
      JwtService jwtService,
      AuthorityRepository authorityRepository) {
    super(Usuario.class, UsuarioServidorResponseDTO.class);
    this.usuarioService = usuarioService;
    this.modelMapper = modelMapper;
    this.jwtService = jwtService;
    this.authorityRepository = authorityRepository;
  }

  @Override
  protected ICrudService<Usuario, Long> getService() {
    return usuarioService;
  }

  @Override
  protected ModelMapper getModelMapper() {
    return modelMapper;
  }

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

  @NotNull
  private ResponseEntity<RespostaLoginDTO> getRespostaLoginDTOResponseEntity(Usuario usuario, Set<Authority> authorities, Authority authority) {
    if (authority == null) {
      return ResponseEntity.badRequest().body(null);
    }
    authorities.add(authority);
    usuario.setAuthorities(authorities);
    Usuario salvo = usuarioService.save(usuario);
    String token = jwtService.generateToken(salvo);
    long expiration = jwtService.getExpirationTime();
    return ResponseEntity.ok(new RespostaLoginDTO(token, expiration));
  }
}
