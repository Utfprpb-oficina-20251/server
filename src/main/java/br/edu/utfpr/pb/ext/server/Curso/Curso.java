package br.edu.utfpr.pb.ext.server.Curso;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_curso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String codigo;
}
