package br.edu.utfpr.pb.ext.server.auth.otp;

import br.edu.utfpr.pb.ext.server.email.EmailCodeValidationService;
import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import br.edu.utfpr.pb.ext.server.usuario.IUsuarioService;
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

  private final EmailCodeValidationService emailCodeValidationService;
  private final IUsuarioService detailsService;

  /**
   * Cria uma instância do provedor de autenticação OTP por e-mail com os serviços de validação de
   * código e acesso a usuários.
   *
   * @param emailCodeValidationService serviço responsável por validar códigos OTP enviados por
   *     e-mail
   * @param detailsService serviço responsável por carregar detalhes do usuário
   */
  public EmailOtpAuthenticationProvider(
      EmailCodeValidationService emailCodeValidationService, IUsuarioService detailsService) {
    this.emailCodeValidationService = emailCodeValidationService;
    this.detailsService = detailsService;
  }

  /**
   * Realiza a autenticação de um usuário utilizando um código OTP enviado por e-mail.
   *
   * <p>Valida o código OTP fornecido para o e-mail informado e, caso seja válido, recupera os
   * detalhes do usuário. Retorna um token de autenticação autenticado com as permissões do usuário.
   *
   * @param authentication objeto contendo o e-mail e o código OTP.
   * @return token de autenticação autenticado com os detalhes e permissões do usuário.
   * @throws BadCredentialsException se o código OTP for inválido ou expirado.
   * @throws UsernameNotFoundException se o usuário não for encontrado pelo e-mail informado.
   */
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    EmailOtpAuthenticationToken authToken = (EmailOtpAuthenticationToken) authentication;
    String email = authToken.getPrincipal().toString();
    String code = authToken.getCredentials().toString();

    boolean isValid =
        emailCodeValidationService.validateCode(email, TipoCodigo.OTP_AUTENTICACAO, code);

    if (!isValid) {
      throw new BadCredentialsException("Código de verificação inválido ou expirado");
    }

    UserDetails userDetails = detailsService.loadUserByUsername(email);
    detailsService.ativarUsuario(email);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  /**
   * Indica se este provedor suporta autenticação usando {@code EmailOtpAuthenticationToken}.
   *
   * @param authentication a classe do token de autenticação a ser verificada
   * @return {@code true} se a autenticação for do tipo {@code EmailOtpAuthenticationToken}
   */
  @Override
  public boolean supports(Class<?> authentication) {
    return EmailOtpAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
