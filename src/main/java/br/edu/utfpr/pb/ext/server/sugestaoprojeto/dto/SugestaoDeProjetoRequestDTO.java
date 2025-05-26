package br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SugestaoDeProjetoRequestDTO {
  @NotBlank @Size(min = 5, max = 100) private String titulo;

  @NotBlank @Size(min = 30, max = 10000) private String descricao;

  @NotBlank @Size(max = 500) private String publicoAlvo;

  private Long professorId;
}
