package br.edu.utfpr.pb.ext.server.inscricao;

import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.*;

@Entity
@Table(name = "tb_inscricao")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Inscricao extends BaseEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "usuario_id")
  @NotNull private Usuario usuario;

  @ManyToOne(optional = false)
  @JoinColumn(name = "projeto_id")
  @NotNull private Projeto projeto;

  @NotNull private Date dataDeInscricao;
}
