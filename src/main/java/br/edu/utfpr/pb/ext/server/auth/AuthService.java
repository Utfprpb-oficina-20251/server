package br.edu.utfpr.pb.ext.server.auth;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.EmailOtpAuthRequestDTO;
import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationProvider;
import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationToken;
import br.edu.utfpr.pb.ext.server.email.impl.EmailServiceImpl;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.authority.Authority;
import br.edu.utfpr.pb.ext.server.usuario.authority.AuthorityRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
  public static final String ALUNOS_UTFPR_EDU_BR = "@alunos.utfpr.edu.br";
  public static final String UTFPR_EDU_BR = "@utfpr.edu.br";
  private final UsuarioRepository usuarioRepository;
  private final AuthorityRepository authorityRepository;
  private final EmailServiceImpl emailService;
  private final EmailOtpAuthenticationProvider emailOtpAuthenticationProvider;

  /**
   * Cadastra um novo usuário atribuindo a autoridade conforme o domínio do e-mail informado.
   *
   * <p>O papel do usuário é determinado automaticamente com base no domínio do e-mail, podendo ser
   * aluno ou servidor. Lança uma exceção com status 400 caso a autoridade correspondente não seja
   * encontrada.
   *
   * @param dto dados para cadastro do usuário, incluindo nome, e-mail e registro
   * @return o usuário cadastrado e persistido no banco de dados
   */
  @Operation(summary = "Cadastra um novo usuário")
  public Usuario cadastro(CadastroUsuarioDTO dto) {
    Usuario usuario =
        Usuario.builder().nome(dto.getNome()).email(dto.getEmail()).cpf(dto.getRegistro()).build();
    Set<Authority> authorities = new HashSet<>();

    String authorityName = obtemRoleDeUsuario(dto);
    Authority authority =
        authorityRepository
            .findByAuthority(authorityName)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao cadastrar"));
    authorities.add(authority);
    usuario.setAuthorities(authorities);
    return usuarioRepository.save(usuario);
  }

  /**
   * Determina o papel do usuário com base no domínio do e-mail informado.
   *
   * <p>Retorna "ROLE_SERVIDOR" se o e-mail termina com "@utfpr.edu.br" ou "ROLE_ALUNO" se termina
   * com "@alunos.utfpr.edu.br". Lança uma exceção se o domínio do e-mail for inválido.
   *
   * @param dto DTO contendo o e-mail do usuário.
   * @return o nome da role correspondente ao domínio do e-mail.
   * @throws ResponseStatusException se o e-mail não pertencer a um domínio permitido.
   */
  @NotNull private static String obtemRoleDeUsuario(CadastroUsuarioDTO dto) {
    final String ROLE_ALUNO = "ROLE_ALUNO";
    final String ROLE_SERVIDOR = "ROLE_SERVIDOR";

    String email = dto.getEmail();
    String authorityName;

    if (email.endsWith(UTFPR_EDU_BR)) {
      authorityName = ROLE_SERVIDOR;
    } else if (email.endsWith(ALUNOS_UTFPR_EDU_BR)) {
      authorityName = ROLE_ALUNO;
    } else {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "E-mail deve ser @utfpr.edu.br ou @alunos.utfpr.edu.br");
    }
    return authorityName;
  }

  /**
   * Envia um código OTP para o email informado, caso o usuário exista.
   *
   * @param email endereço de email do usuário que receberá o código OTP
   * @return true se o código foi enviado com sucesso
   * @throws ResponseStatusException se o email não estiver cadastrado ou ocorrer erro no envio do
   *     código
   */
  @Operation(summary = "Solicita um código OTP para autenticação via email")
  public boolean solicitarCodigoOtp(String email) {
    logger.info("Solicitação de código para validação de email");
    try {
      // Verificar se o usuário existe
      usuarioRepository
          .findByEmail(email)
          .orElseThrow(() -> new UsernameNotFoundException("Email não cadastrado"));
      emailService.generateAndSendCode(email, "autenticacao");
      logger.info("Código de verificação enviado");
      return true;
    } catch (Exception e) {
      logger.error("Erro ao enviar código de verificação", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao enviar código de verificação");
    }
  }

  /**
   * Realiza a autenticação de um usuário utilizando um código OTP enviado por email.
   *
   * @param dto Objeto contendo o email do usuário e o código OTP recebido.
   * @return O usuário autenticado correspondente ao email informado.
   * @throws ResponseStatusException Se o código OTP for inválido ou expirado, retorna erro 401.
   * @throws UsernameNotFoundException Se o email informado não estiver cadastrado.
   */
  @Operation(summary = "Autentica um usuário usando o código OTP")
  public Usuario autenticacaoOtp(EmailOtpAuthRequestDTO dto) {
    try {
      EmailOtpAuthenticationToken authToken =
          new EmailOtpAuthenticationToken(dto.getEmail(), dto.getCode());
      Authentication authentication = emailOtpAuthenticationProvider.authenticate(authToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      return usuarioRepository
          .findByEmail(dto.getEmail())
          .orElseThrow(() -> new UsernameNotFoundException("Email não cadastrado"));
    } catch (BadCredentialsException ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código inválido ou expirado");
    }
  }
}
