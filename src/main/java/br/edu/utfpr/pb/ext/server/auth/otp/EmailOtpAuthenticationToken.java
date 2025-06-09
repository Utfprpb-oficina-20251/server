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
   * @param principal identidade do usuário, geralmente o e-mail
   * @param credentials código OTP ou senha temporária utilizada para autenticação
   */
  public EmailOtpAuthenticationToken(Object principal, Object credentials) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    setAuthenticated(false);
  }

  /**
   * Inicializa um token de autenticação autenticado para autenticação via OTP por e-mail.
   *
   * @param principal identidade do usuário, como e-mail ou nome de usuário
   * @param credentials código OTP ou senha temporária fornecida pelo usuário
   * @param authorities coleções de permissões concedidas ao usuário autenticado
   */
  public EmailOtpAuthenticationToken(
      Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(true);
  }

  /**
   * Retorna as credenciais associadas a este token de autenticação, geralmente o código OTP
   * informado pelo usuário.
   *
   * @return as credenciais fornecidas para autenticação
   */
  @Override
  public Object getCredentials() {
    return this.credentials;
  }

  /**
   * Retorna o principal associado a este token de autenticação, geralmente representando a
   * identidade do usuário (por exemplo, e-mail).
   *
   * @return o principal, normalmente o e-mail do usuário
   */
  @Override
  public Object getPrincipal() {
    return this.principal;
  }

  /**
   * Remove as credenciais sensíveis deste token, definindo-as como nulas.
   *
   * <p>Este método é utilizado para garantir que informações confidenciais, como senhas ou OTPs,
   * não permaneçam em memória após o processo de autenticação.
   */
  @Override
  public void eraseCredentials() {
    super.eraseCredentials();
    this.credentials = null;
  }
}
