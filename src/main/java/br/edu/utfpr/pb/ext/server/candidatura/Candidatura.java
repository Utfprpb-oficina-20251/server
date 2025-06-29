package br.edu.utfpr.pb.ext.server.candidatura;

import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "tb_candidatura")
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

  private LocalDateTime dataCandidatura;
}
