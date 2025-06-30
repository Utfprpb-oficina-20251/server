package br.edu.utfpr.pb.ext.server.candidatura;

import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "tb_candidatura",
    uniqueConstraints = @UniqueConstraint(columnNames = {"projeto_id", "aluno_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidatura extends BaseEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "aluno_id")
  private Usuario aluno;

  @ManyToOne(optional = false)
  @JoinColumn(name = "projeto_id")
  private Projeto projeto;

  @CreatedDate
  @Column(nullable = false)
  private LocalDateTime dataCandidatura;

  @Enumerated(EnumType.STRING)
  private StatusCandidatura status;
}
