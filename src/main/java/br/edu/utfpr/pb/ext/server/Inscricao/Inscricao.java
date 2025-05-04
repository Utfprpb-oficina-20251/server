package br.edu.utfpr.pb.ext.server.Inscricao;

import br.edu.utfpr.pb.ext.server.Projeto.Projeto;
import br.edu.utfpr.pb.ext.server.Usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Entity
@Table(name="tb_inscricao")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Inscricao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @NotNull
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "projeto_id")
    @NotNull
    private Projeto projeto;

    @NotNull
    private Date dataDeInscricao;
}
