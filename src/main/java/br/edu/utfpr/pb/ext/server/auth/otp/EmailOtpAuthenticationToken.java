package br.edu.utfpr.pb.ext.server.auth.otp;

import java.util.Collection;
import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

@EqualsAndHashCode(callSuper = true)
public class EmailOtpAuthenticationToken extends AbstractAuthenticationToken {
  private final transient Object principal;
  private transient Object credentials;

  /**
   * Cria um token de autenticação de OTP por e-mail não autenticado, contendo o principal e as
   * credenciais fornecidas.
   *
   * @param principal identidade do usuário (por exemplo, e-mail)
   * @param credentials código OTP ou senha temporária
   */
  public EmailOtpAuthenticationToken(Object principal, Object credentials) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    setAuthenticated(false);
  }

  /**
     * Instancia um token de autenticação já autenticado para autenticação via OTP por e-mail.
     *
     * @param principal identidade do usuário, como e-mail ou nome de usuário
     * @param credentials código OTP ou credencial temporária associada ao usuário
     * @param authorities coleção de permissões concedidas ao usuário autenticado
     */
  public EmailOtpAuthenticationToken(
      Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(true);
  }

  /**
   * Retorna as credenciais (por exemplo, o código OTP) associadas a este token de autenticação.
   *
   * @return o valor das credenciais fornecidas para autenticação
   */
  @Override
  public Object getCredentials() {
    return this.credentials;
  }

  /**
   * Retorna o principal associado a este token de autenticação, geralmente representando a
   * identidade do usuário (por exemplo, e-mail).
   *
   * @return o principal deste token de autenticação
   */
  @Override
  public Object getPrincipal() {
    return this.principal;
  }

  /**
   * Remove as credenciais sensíveis deste token, definindo-as como nulas.
   *
   * <p>Garante que informações confidenciais, como senhas ou OTPs, sejam eliminadas da memória após a autenticação.
   */
  @Override
  public void eraseCredentials() {
    super.eraseCredentials();
    this.credentials = null;
  }
}
