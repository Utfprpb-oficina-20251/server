package br.edu.utfpr.pb.ext.server.Curso;

import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "tb_curso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso extends BaseEntity {

    @NotNull
    private String nome;
    @NotNull
    private String codigo;
}
