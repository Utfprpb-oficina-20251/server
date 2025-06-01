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
   * Inicializa um token de autenticação de OTP por e-mail em estado não autenticado, armazenando o principal e as credenciais fornecidas.
   *
   * @param principal identidade do usuário, como o endereço de e-mail
   * @param credentials código OTP ou senha temporária associada à autenticação
   */
  public EmailOtpAuthenticationToken(Object principal, Object credentials) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    setAuthenticated(false);
  }

  /**
   * Cria um token de autenticação autenticado para autenticação via OTP por e-mail.
   *
   * @param principal identidade do usuário, como e-mail ou nome de usuário
   * @param credentials código OTP ou credencial temporária do usuário
   * @param authorities permissões concedidas ao usuário autenticado
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
   * Garante que informações confidenciais, como senhas ou OTPs, sejam eliminadas da memória após a autenticação.
   */
  @Override
  public void eraseCredentials() {
    super.eraseCredentials();
    this.credentials = null;
  }
}
