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
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
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

  @PostMapping("/servidor")
  public ResponseEntity<RespostaLoginDTO> createServidor(
      @Valid @RequestBody UsuarioServidorRequestDTO usuarioServidorRequestDTO) {
    Usuario usuario = modelMapper.map(usuarioServidorRequestDTO, Usuario.class);
    Set<Authority> authorities = new HashSet<>();
    Authority servidorAuthority = authorityRepository.findByAuthority("ROLE_SERVIDOR").orElse(null);
    if (servidorAuthority == null) {
      return ResponseEntity.badRequest().body(null);
    }
    authorities.add(servidorAuthority);
    usuario.setAuthorities(authorities);
    Usuario salvo = usuarioService.save(usuario);
    String token = jwtService.generateToken(salvo);
    long expiration = jwtService.getExpirationTime();
    return ResponseEntity.ok(new RespostaLoginDTO(token, expiration));
  }

  @PostMapping("/aluno")
  public ResponseEntity<RespostaLoginDTO> createAluno(
      @Valid @RequestBody UsuarioAlunoRequestDTO usuarioAlunoRequestDTO) {
    Usuario usuario = modelMapper.map(usuarioAlunoRequestDTO, Usuario.class);
    Set<Authority> authorities = new HashSet<>();
    Authority alunoAuthority = authorityRepository.findByAuthority("ROLE_ALUNO").orElse(null);
    if (alunoAuthority == null) {
      return ResponseEntity.badRequest().body(null);
    }
    authorities.add(alunoAuthority);
    usuario.setAuthorities(authorities);
    Usuario salvo = usuarioService.save(usuario);
    String token = jwtService.generateToken(salvo);
    long expiration = jwtService.getExpirationTime();
    return ResponseEntity.ok(new RespostaLoginDTO(token, expiration));
  }
}
