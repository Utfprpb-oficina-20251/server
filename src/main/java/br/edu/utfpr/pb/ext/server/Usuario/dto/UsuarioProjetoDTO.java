package br.edu.utfpr.pb.ext.server.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioProjetoDTO {
    private Long id;

    @NotNull
    private String nomeCompleto;

    @NotNull
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@(utfpr\\.edu\\.br)$")
    private String email;
}
