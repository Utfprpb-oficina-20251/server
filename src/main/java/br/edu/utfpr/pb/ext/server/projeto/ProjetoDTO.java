package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioProjetoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class ProjetoDTO {

  private Long id;

  @NotNull @Size(min = 5, max = 100) private String titulo;

  @NotNull @Size(min = 20, max = 500) private String descricao;

  @NotNull @Size(min = 20, max = 500) private String justificativa;

  @NotNull @JsonFormat(pattern="yyyy-MM-dd") private Date dataInicio;

  @NotNull @JsonFormat(pattern="yyyy-MM-dd") private Date dataFim;

  @NotNull @Size(max = 500) private String publicoAlvo;

  @NotNull private boolean vinculadoDisciplina;

  @NotNull @Size(max = 500) private String restricaoPublico;

  @NotNull private List<UsuarioProjetoDTO> equipeExecutora;

  private StatusProjeto status;
}
