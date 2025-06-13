package br.edu.utfpr.pb.ext.server.email.enums;

import lombok.Getter;

@Getter
public enum TipoCodigo {
  OTP_AUTENTICACAO("autenticacao"),
  OTP_CADASTRO("cadastro"),
  OTP_RECUPERACAO("recuperacao");

  private final String tipo;

  TipoCodigo(String tipo) {
    this.tipo = tipo;
  }
}
