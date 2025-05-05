package br.edu.utfpr.pb.ext.server.sugestaoDeProjeto;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "tb_sugestao_de_projeto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SugestaoDeProjeto extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "aluno_id")
  @NotNull private Usuario aluno;

  @ManyToOne
  @JoinColumn(name = "professor_id")
  @NotNull private Usuario professor;

  @ManyToOne
  @JoinColumn(name = "curso_id")
  @NotNull private Curso curso;

  @NotNull private String descricao;
}
