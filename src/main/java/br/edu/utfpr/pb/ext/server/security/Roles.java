package br.edu.utfpr.pb.ext.server.security;

// Enumera e padroniza os roles e garante o acesso de forma correta

public enum Roles{
    ADMINISTRADOR("ADMINISTRADOR"),
    SERVIDOR("SERVIDOR"),
    ESTUDANTE("ESTUDANTE");

    private final String authority;

    Roles(String role) {
        this.authority = role;
    }

    public String getAuthority() {
        return authority;
    }
}