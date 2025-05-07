package br.edu.utfpr.pb.ext.server.usuario.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;

@Data
public class UsuarioServidorRequestDTO {

    private Long id;

    @NotNull
    private String nomeCompleto;

    @NotNull
    @CPF
    private String cpf;

    @NotNull
    private String siape;

    @NotNull
    @Email(message = "Endereço de e-mail inválido", regexp = "^[a-zA-Z0-9._%+-]+@(utfpr\\.edu\\.br)$")
    private String emailInstitucional;

    @Size(min = 11, max = 11)
    private String telefone;

    @Size(min = 3, max = 100)
    private String enderecoCompleto;

    @NotNull
    private String departamento;
}
