package br.edu.utfpr.pb.ext.server.SujestaoDeProjeto;

import br.edu.utfpr.pb.ext.server.Curso.Curso;
import br.edu.utfpr.pb.ext.server.Usuario.Usuario;
import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name="tb_sujestao_de_projeto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SujestaoDeProjeto extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    @NotNull
    private Usuario aluno;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    @NotNull
    private Usuario professor;

    @ManyToOne
    @JoinColumn(name = "curso_id")
    @NotNull
    private Curso curso;
}
