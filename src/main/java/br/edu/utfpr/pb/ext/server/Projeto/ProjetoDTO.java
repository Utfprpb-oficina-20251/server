package br.edu.utfpr.pb.ext.server.Projeto;

import br.edu.utfpr.pb.ext.server.enums.StatusProjeto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
public class ProjetoDTO {

    private Long id;

    @NotNull
    @Size(min = 5, max = 100)
    private String titulo;

    @NotNull
    @Size(min = 20, max = 500)
    private String descricao;

    @NotNull
    @Size(min = 20, max = 500)
    private String justificativa;

    @NotNull
    private Date dataInicio;

    @NotNull
    private Date dataFim;

    @NotNull
    @Size(max = 500)
    private String publicoAlvo;

    @NotNull
    private boolean vinculadoDisciplina;

    @NotNull
    @Size(max = 500)
    private String restricaoPublico;

    @NotNull
    private String equipeExecutora;

    @Size(min = 10, max = 11)
    private String telefoneOrientador;

    @NotNull
    private StatusProjeto status;
}
