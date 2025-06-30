package br.edu.utfpr.pb.ext.server.candidatura;

public enum StatusCandidatura {
  APROVADA("Aceita"),
  CANCELADA("Cancelada"),
  PENDENTE("Pendente"),
  REJEITADA("Rejeitada");

  private final String descricao;

  StatusCandidatura(String descricao) {
    this.descricao = descricao;
  }

  public String getDescricao() {
    return descricao;
  }
}
