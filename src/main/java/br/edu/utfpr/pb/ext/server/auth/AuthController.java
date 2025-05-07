package br.edu.utfpr.pb.ext.server.auth;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.LoginUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.RespostaLoginDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.UsuarioCadastradoDTO;
import br.edu.utfpr.pb.ext.server.auth.jwt.JwtService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final JwtService jwtService;
  private final AuthService authService;
  private final ModelMapper modelMapper;

  @PostMapping("/cadastro")
  public ResponseEntity<UsuarioCadastradoDTO> cadastro(
      @RequestBody CadastroUsuarioDTO cadastroUsuarioDTO) {
    Usuario usuarioRegistrado = authService.cadastro(cadastroUsuarioDTO);
    return ResponseEntity.ok(modelMapper.map(usuarioRegistrado, UsuarioCadastradoDTO.class));
  }

  @PostMapping("/login")
  public ResponseEntity<RespostaLoginDTO> autenticacao(
      @RequestBody LoginUsuarioDTO loginUsuarioDTO) {
    Usuario usuarioAutenticado = authService.autenticacao(loginUsuarioDTO);
    String tokenJwt = jwtService.generateToken(usuarioAutenticado);
    RespostaLoginDTO respostaLoginDTO =
        RespostaLoginDTO.builder()
            .token(tokenJwt)
            .expiresIn(jwtService.getExpirationTime())
            .build();
    return ResponseEntity.ok(respostaLoginDTO);
  }
}
