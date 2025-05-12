package br.edu.utfpr.pb.ext.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Dados de cadastro de usuário")
public class CadastroUsuarioDTO {
  @Schema(description = "E-mail do usuário", example = "email@email.com")
  private @Email String email;

  @Schema(description = "Nome do usuário", example = "Jão das neves")
  private @NotBlank String nome;

  @Schema(description = "Registro do usuário", example = "123456789")
  private @NotBlank String registro;
}
