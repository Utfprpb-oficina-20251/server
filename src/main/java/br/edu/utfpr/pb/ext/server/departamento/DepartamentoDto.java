package br.edu.utfpr.pb.ext.server.departamento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO utilizado para transferir dados da entidade Departamento
 * entre as camadas da aplicação (Controller, Service, etc.).
 */
@Getter
@Setter
public class DepartamentoDto {

  /**
   * Identificador único do departamento.
   */
  private Long id;

  /**
   * Sigla do departamento (ex: DAINF, DAADM).
   */
  @NotBlank(message = "Sigla é obrigatória")
  @Size(max = 20, message = "Sigla deve ter no máximo 20 caracteres")
  private String sigla;

  /**
   * Nome completo do departamento.
   */
  @NotBlank(message = "Nome é obrigatório")
  private String nome;

  /**
   * ID do usuário responsável pelo departamento.
   */
  private Long responsavelId;
}