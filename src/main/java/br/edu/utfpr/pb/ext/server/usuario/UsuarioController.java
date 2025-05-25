package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.auth.dto.RespostaLoginDTO;
import br.edu.utfpr.pb.ext.server.auth.jwt.JwtService;
import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorRequestDTO;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorResponseDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController extends CrudController<Usuario, UsuarioServidorResponseDTO, Long> {
  private final IUsuarioService usuarioService;
  private final ModelMapper modelMapper;
  private final JwtService jwtService;
  private final UsuarioRepository usuarioRepository;

  public UsuarioController(
      IUsuarioService usuarioService,
      ModelMapper modelMapper,
      JwtService jwtService,
      UsuarioRepository usuarioRepository) {
    super(Usuario.class, UsuarioServidorResponseDTO.class);
    this.usuarioService = usuarioService;
    this.modelMapper = modelMapper;
    this.jwtService = jwtService;
    this.usuarioRepository = usuarioRepository;
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
    usuario.getRoles().add("SERVIDOR");
    usuario.getAuthorities();
    Usuario salvo = usuarioService.save(usuario);
    Map<String, Object> authorities = Map.of("authority", salvo.getAuthorities());
    String token = jwtService.generateToken(authorities, salvo);
    long expiration = jwtService.getExpirationTime();
    return ResponseEntity.ok(new RespostaLoginDTO(token, expiration));
  }

  @GetMapping("/servidores-ativos")
  public ResponseEntity<List<UsuarioServidorResponseDTO>> listarServidoresAtivos() {
    List<Usuario> servidores = usuarioRepository.findServidoresAtivos(); // Usa o novo método
    return ResponseEntity.ok(
        servidores.stream()
            .map(servidor -> modelMapper.map(servidor, UsuarioServidorResponseDTO.class))
            .toList());
  }
}
