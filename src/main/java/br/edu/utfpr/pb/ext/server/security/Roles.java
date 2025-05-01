package br.edu.utfpr.pb.ext.server.security;

import lombok.Getter;

// Enumera e padroniza os roles e garante o acesso de forma correta
@Getter
public enum Roles {
  ADMINISTRADOR("ADMINISTRADOR"),
  SERVIDOR("SERVIDOR"),
  ESTUDANTE("ESTUDANTE");

  private final String authority;

  Roles(String role) {
    this.authority = role;
  }
}
