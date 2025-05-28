package br.edu.utfpr.pb.ext.server.curso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CursoDTO {
  private Long id;
  private String nome;
  private String codigo;
}
