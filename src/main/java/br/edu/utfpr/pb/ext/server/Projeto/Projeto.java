package br.edu.utfpr.pb.ext.server.Projeto;


import br.edu.utfpr.pb.ext.server.Usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tb_projeto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projeto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String descricao;

    private String justificativa;

    private Date dataInicio;

    private Date dataFim;

    private String publicoAlvo;

    private boolean vinculadoDisciplina;

    private String restricaoPublico;

    @ManyToMany
    @JoinTable(
            name = "tb_equipe_servidor",
            joinColumns = @JoinColumn(name = "id_projeto"),
            inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    private List<Usuario> EquipeExecutora;
}
