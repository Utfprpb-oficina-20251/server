package br.edu.utfpr.pb.ext.server.notificacao.enums;

import lombok.Getter;

@Getter
public enum TipoReferencia {
  SUGESTAO_PROJETO("Sugestão de Projeto"),
  CANDIDATURA("Candidatura"),
  PROJETO("Projeto"),
  AVISO("Aviso"),
  SISTEMA("Sistema");

  private final String displayName;

  TipoReferencia(String displayName) {
    this.displayName = displayName;
  }
}
