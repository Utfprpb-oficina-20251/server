package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;

import lombok.*;

@Entity
@Table(name = "tb_projeto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projeto extends BaseEntity {

    @NotNull
    private String titulo;

    @NotNull
    private String descricao;

    @NotNull
    private String justificativa;

    @NotNull
    private Date dataInicio;

    private Date dataFim;

    @NotNull
    private String publicoAlvo;

    @NotNull
    private boolean vinculadoDisciplina;

    private String restricaoPublico;

    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    @ManyToMany
    @JoinTable(
            name = "tb_equipe_servidor",
            joinColumns = @JoinColumn(name = "id_projeto"),
            inverseJoinColumns = @JoinColumn(name = "id_usuario"))
    private List<Usuario> equipeExecutora;

  @NotNull private StatusProjeto status;

  @Column(length = 1000)
  private String justificativaCancelamento;

    @Column(name = "carga_horaria")
    private Long cargaHoraria;

    @Column(name = "qtde_vagas")
    private Long qtdeVagas;
}
