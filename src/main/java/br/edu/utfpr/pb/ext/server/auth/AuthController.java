package br.edu.utfpr.pb.ext.server.auth;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.LoginUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.RespostaLoginDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.UsuarioCadastradoDTO;
import br.edu.utfpr.pb.ext.server.auth.jwt.JwtService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.validation.Valid;
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

  /**
   * Registra um novo usuário a partir dos dados fornecidos e retorna as informações do usuário
   * cadastrado.
   *
   * @param cadastroUsuarioDTO dados para cadastro do novo usuário
   * @return resposta HTTP contendo o DTO do usuário cadastrado
   */
  @PostMapping("/cadastro")
  public ResponseEntity<UsuarioCadastradoDTO> cadastro(
      @RequestBody @Valid CadastroUsuarioDTO cadastroUsuarioDTO) {
    Usuario usuarioRegistrado = authService.cadastro(cadastroUsuarioDTO);
    return ResponseEntity.ok(modelMapper.map(usuarioRegistrado, UsuarioCadastradoDTO.class));
  }

  /**
   * Autentica um usuário e retorna um token JWT com o tempo de expiração.
   *
   * <p>Recebe as credenciais de login, autentica o usuário e gera um token JWT para acesso
   * autenticado.
   *
   * @param loginUsuarioDTO dados de login do usuário
   * @return resposta contendo o token JWT e o tempo de expiração
   */
  @PostMapping("/login")
  public ResponseEntity<RespostaLoginDTO> autenticacao(
      @RequestBody @Valid LoginUsuarioDTO loginUsuarioDTO) {
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
