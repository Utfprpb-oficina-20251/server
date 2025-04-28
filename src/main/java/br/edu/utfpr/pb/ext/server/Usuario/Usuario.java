package br.edu.utfpr.pb.ext.server.Usuario;

import br.edu.utfpr.pb.ext.server.Curso.Curso;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="tb_usuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String registro;

    private String email;

    private String telefone;

    private boolean professor;

    private boolean administrador;

    private String senha;

    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso curso;
}
