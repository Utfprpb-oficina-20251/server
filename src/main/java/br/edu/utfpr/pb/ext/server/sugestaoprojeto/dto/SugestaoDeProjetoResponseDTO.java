package br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto;

import br.edu.utfpr.pb.ext.server.sugestaoprojeto.StatusSugestao;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SugestaoDeProjetoResponseDTO {
  private Long id;
  private String titulo;
  private String descricao;
  private String publicoAlvo;

  private Long alunoId;
  private String alunoNome;

  private Long professorId;
  private String professorNome;

  private StatusSugestao status;
  private LocalDateTime dataCriacao;
}
