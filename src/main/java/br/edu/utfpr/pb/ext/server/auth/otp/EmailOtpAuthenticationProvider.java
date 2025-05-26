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

  public EmailOtpAuthenticationProvider(
      EmailCodeValidationService emailCodeValidationService, UsuarioRepository usuarioRepository) {
    this.emailCodeValidationService = emailCodeValidationService;
    this.usuarioRepository = usuarioRepository;
  }

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

  @Override
  public boolean supports(Class<?> authentication) {
    return EmailOtpAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
