package br.edu.utfpr.pb.ext.server.projeto.enums;

import lombok.Getter;

@Getter
public enum StatusProjeto {
  EM_ANDAMENTO("Em Andamento"),
  CONCLUIDO("Concluído"),
  CANCELADO("Cancelado"),
  FINALIZADO("Finalizado");

  private final String descricao;

  /**
   * Cria uma nova instância do status do projeto com a descrição fornecida.
   *
   * @param descricao descrição associada ao status do projeto
   */
  StatusProjeto(String descricao) {
    this.descricao = descricao;
  }
}
