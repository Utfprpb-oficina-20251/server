package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.departamento.enums.Departamentos;
import lombok.Data;

/**
 * Data Transfer Object (DTO) utilizado para transferência de dados entre o cliente e o servidor na
 * associação de um responsável a um departamento.
 */
@Data
public class DepartamentoDTO {

  /** Identificador único da associação (usado em atualizações). */
  private Long id;

  /**
   * Enum que representa o departamento associado. Utiliza o enum {@link Departamentos} para
   * padronização dos valores possíveis.
   */
  private Departamentos departamento;

  /** ID do usuário responsável pelo departamento. */
  private Long responsavelId;
}
