package br.edu.utfpr.pb.ext.server.auth.otp;

import br.edu.utfpr.pb.ext.server.email.EmailCodeValidationService;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class EmailOtpAuthenticationProvider implements AuthenticationProvider {

  public static final String AUTENTICACAO = "autenticacao";
  private final EmailCodeValidationService emailCodeValidationService;
  private final UsuarioRepository usuarioRepository;

  /**
   * Cria uma instância do provedor de autenticação OTP por e-mail.
   *
   * @param emailCodeValidationService serviço responsável por validar códigos OTP enviados por
   *     e-mail
   * @param usuarioRepository repositório para busca de usuários pelo e-mail
   */
  public EmailOtpAuthenticationProvider(
      EmailCodeValidationService emailCodeValidationService, UsuarioRepository usuarioRepository) {
    this.emailCodeValidationService = emailCodeValidationService;
    this.usuarioRepository = usuarioRepository;
  }

  /**
   * Realiza a autenticação de um usuário utilizando um código OTP enviado por e-mail.
   *
   * <p>Valida o código OTP fornecido para o e-mail informado e, se válido, recupera os detalhes do
   * usuário. Em caso de sucesso, retorna um token de autenticação com as autoridades do usuário.
   *
   * @param authentication objeto contendo o e-mail e o código OTP.
   * @return um token de autenticação autenticado com os detalhes do usuário.
   * @throws BadCredentialsException se o código OTP for inválido ou expirado.
   * @throws UsernameNotFoundException se o usuário não for encontrado pelo e-mail informado.
   */
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    EmailOtpAuthenticationToken authToken = (EmailOtpAuthenticationToken) authentication;
    String email = authToken.getPrincipal().toString();
    String code = authToken.getCredentials().toString();

    // Validate the OTP code for the "autenticacao" type
    boolean isValid = emailCodeValidationService.validateCode(email, AUTENTICACAO, code);

    if (!isValid) {
      throw new BadCredentialsException("Código de verificação inválido ou expirado");
    }

    // Find the user by email
    UserDetails userDetails =
        usuarioRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

    // Create an authentication token with the user's authorities
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  /**
   * Verifica se este provedor de autenticação suporta o tipo de autenticação fornecido.
   *
   * @param authentication classe do token de autenticação a ser verificado
   * @return {@code true} se o tipo de autenticação for compatível com {@code
   *     EmailOtpAuthenticationToken}
   */
  @Override
  public boolean supports(Class<?> authentication) {
    return EmailOtpAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
