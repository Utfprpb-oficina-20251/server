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
   * Registra um novo usuário e retorna os dados do usuário cadastrado.
   *
   * <p>Recebe os dados de cadastro, cria o usuário e retorna as informações do usuário cadastrado.
   *
   * @param cadastroUsuarioDTO informações para cadastro do novo usuário
   * @return resposta HTTP com os dados do usuário cadastrado
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
   * Solicita o envio de um código OTP para o endereço de email fornecido.
   *
   * <p>Gera e envia um código de uso único (OTP) para o email informado, permitindo a autenticação
   * do usuário por meio desse código.
   *
   * @param email Endereço de email que receberá o código OTP.
   * @return Objeto contendo mensagem de confirmação do envio do código.
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
   * Autentica um usuário por meio de email e código OTP, retornando um token JWT e o tempo de
   * expiração.
   *
   * @param requestDTO objeto contendo o email do usuário e o código OTP para autenticação
   * @return resposta com o token JWT gerado e o tempo de expiração em segundos
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
