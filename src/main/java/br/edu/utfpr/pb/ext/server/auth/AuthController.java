package br.edu.utfpr.pb.ext.server.auth;

import br.edu.utfpr.pb.ext.server.auth.dto.*;
import br.edu.utfpr.pb.ext.server.auth.jwt.JwtService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
   * Solicita o envio de um código OTP para o email do usuário.
   *
   * @param email Email do usuário
   * @return Resposta HTTP indicando sucesso ou falha
   */
  @Operation(summary = "Solicita um código OTP para autenticação")
  @ApiResponse(responseCode = "200", description = "Código enviado com sucesso")
  @PostMapping("/solicitar-codigo")
  public ResponseEntity<SolicitacaoCodigoDTO> solicitarCodigoOtp(
      @RequestParam @Email String email) {
    authService.solicitarCodigoOtp(email);
    return ResponseEntity.ok(
        SolicitacaoCodigoDTO.builder()
            .mensagem("Código de verificação enviado para " + email)
            .build());
  }

  /**
   * Autentica um usuário usando o código OTP e retorna um token JWT.
   *
   * @param requestDTO DTO contendo email e código OTP
   * @return ResponseEntity com o token JWT e tempo de expiração
   */
  @Operation(summary = "Autentica um usuário usando OTP")
  @ApiResponse(
      responseCode = "200",
      description = "Usuário autenticado com sucesso",
      content = @Content(schema = @Schema(implementation = RespostaLoginDTO.class)))
  @PostMapping("/login-otp")
  public ResponseEntity<RespostaLoginDTO> autenticacaoOtp(
      @RequestBody @Valid EmailOtpAuthRequestDTO requestDTO) {
    Usuario usuarioAutenticado = authService.autenticacaoOtp(requestDTO);
    String tokenJwt = jwtService.generateToken(usuarioAutenticado);
    RespostaLoginDTO respostaLoginDTO =
        RespostaLoginDTO.builder()
            .token(tokenJwt)
            .expiresIn(jwtService.getExpirationTime())
            .build();
    return ResponseEntity.ok(respostaLoginDTO);
  }
}
