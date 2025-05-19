package br.edu.utfpr.pb.ext.server.auth;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.LoginUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.RespostaLoginDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.UsuarioCadastradoDTO;
import br.edu.utfpr.pb.ext.server.auth.jwt.JwtService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Autenticação",
    description = "Endpoints responsáveis por cadastro e autenticação de usuário")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final JwtService jwtService;
  private final AuthService authService;
  private final ModelMapper modelMapper;

  /**
   * Realiza o cadastro de um novo usuário e retorna os dados do usuário cadastrado.
   *
   * @param cadastroUsuarioDTO informações necessárias para registrar o novo usuário
   * @return resposta HTTP com o DTO do usuário cadastrado
   */
  @Operation(summary = "Cadastra um novo usuário")
  @ApiResponse(
      responseCode = "200",
      description = "Usuário cadastrado com sucesso",
      content = @Content(schema = @Schema(implementation = UsuarioCadastradoDTO.class)))
  @PostMapping("/cadastro")
  public ResponseEntity<UsuarioCadastradoDTO> cadastro(
      @RequestBody @Valid CadastroUsuarioDTO cadastroUsuarioDTO) {
    Usuario usuarioRegistrado = authService.cadastro(cadastroUsuarioDTO);
    return ResponseEntity.ok(modelMapper.map(usuarioRegistrado, UsuarioCadastradoDTO.class));
  }

  /**
   * Realiza a autenticação do usuário e retorna um token JWT com o tempo de expiração.
   *
   * <p>Recebe as credenciais de login, autentica o usuário e, em caso de sucesso, gera um token JWT
   * para acesso autenticado, incluindo o tempo de expiração do token na resposta.
   *
   * @param loginUsuarioDTO objeto contendo as credenciais do usuário para autenticação
   * @return ResponseEntity com o token JWT e o tempo de expiração em caso de autenticação
   *     bem-sucedida
   */
  @Operation(summary = "Autentica um usuário")
  @ApiResponse(
      responseCode = "200",
      description = "Usuário autenticado com sucesso",
      content = @Content(schema = @Schema(implementation = RespostaLoginDTO.class)))
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
