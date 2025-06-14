package br.edu.utfpr.pb.ext.server.email.enums;

import lombok.Getter;

@Getter
public enum TipoCodigo {
  OTP_AUTENTICACAO("autenticacao"),
  OTP_CADASTRO("cadastro"),
  OTP_RECUPERACAO("recuperacao");

  private final String tipo;

  /**
   * Inicializa a constante do enum com o valor de tipo associado.
   *
   * @param tipo valor de string que representa o tipo do código
   */
  TipoCodigo(String tipo) {
    this.tipo = tipo;
  }
}
