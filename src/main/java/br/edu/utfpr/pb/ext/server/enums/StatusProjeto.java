package br.edu.utfpr.pb.ext.server.enums;

import lombok.Getter;

@Getter
public enum StatusProjeto {
  EM_ANDAMENTO("Em Andamento"),
  CONCLUIDO("Concluído"),
  CANCELADO("Cancelado"),
  FINALIZADO("Finalizado");

  private final String descricao;

  StatusProjeto(String descricao) {
    this.descricao = descricao;
  }
}
