package br.edu.utfpr.pb.ext.server.email.enums;

import lombok.Getter;

@Getter
public enum TipoCodigo {
  OTP_AUTENTICACAO("Autenticação"),
  OTP_CADASTRO("Cadastro"),
  OTP_RECUPERACAO("Recuperação de Senha"),
  ;

  private final String tipo;

  /**
   * Inicializa a constante do enum com o valor de tipo associado.
   *
   * @param tipo valor de string que representa o tipo do código
   */
  TipoCodigo(String tipo) {
    this.tipo = tipo;
  }

  /**
   * Retorna o tipo do código como uma string.
   *
   * @return o tipo do código
   */
  public String getTipo() {
    return tipo;
  }
}
