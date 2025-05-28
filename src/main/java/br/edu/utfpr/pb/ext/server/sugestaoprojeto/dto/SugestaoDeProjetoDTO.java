package br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto;

import br.edu.utfpr.pb.ext.server.curso.dto.CursoDTO;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.StatusSugestao;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioNomeIdDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SugestaoDeProjetoDTO {
  private Long id;

  @NotBlank @Size(min = 5, max = 100) private String titulo;

  @NotBlank @Size(min = 30, max = 10000) private String descricao;

  @NotBlank @Size(max = 500) private String publicoAlvo;

  private UsuarioNomeIdDTO aluno;
  private UsuarioNomeIdDTO professor;
  private StatusSugestao status;
  private LocalDateTime dataCriacao;
  @NotNull private CursoDTO curso;
}
