package br.edu.utfpr.pb.ext.server.SujestaoDeProjeto;

import br.edu.utfpr.pb.ext.server.Curso.Curso;
import br.edu.utfpr.pb.ext.server.Usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="tb_sujestao_de_projeto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SujestaoDeProjeto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Usuario aluno;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Usuario professor;

    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso curso;
}
