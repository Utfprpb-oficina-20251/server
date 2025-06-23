package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(name = "tb_sugestao_de_projeto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SugestaoDeProjeto extends BaseEntity {

  @NotBlank @Size(min = 5, max = 100) private String titulo;

  @NotBlank @Size(min = 30) private String descricao;

  @NotBlank @Size(max = 500) private String publicoAlvo;

  @ManyToOne
  @JoinColumn(name = "aluno_id")
  @NotNull private Usuario aluno;

  @ManyToOne
  @JoinColumn(name = "professor_id")
  private Usuario professor;

  @URL
  @Column(length = 2048)
  private String imagemUrl;

  @Enumerated(EnumType.STRING)
  private StatusSugestao status;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime dataCriacao;

  @ManyToOne
  @JoinColumn(name = "curso_id")
  @NotNull private Curso curso;
}
