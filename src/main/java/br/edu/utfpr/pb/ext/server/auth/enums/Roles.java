package br.edu.utfpr.pb.ext.server.auth.enums;

import lombok.Getter;

// Enumera e padroniza os roles e garante o acesso de forma correta
@Getter
public enum Roles {
  ADMINISTRADOR("ADMINISTRADOR"),
  SERVIDOR("SERVIDOR"),
  ESTUDANTE("ESTUDANTE");

  private final String authority;

  /**
   * Inicializa uma constante do enum Roles com o nome da autoridade fornecido.
   *
   * @param role nome da autoridade associado ao papel
   */
  Roles(String role) {
    this.authority = role;
  }
}
