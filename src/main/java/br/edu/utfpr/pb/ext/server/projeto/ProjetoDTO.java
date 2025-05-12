
package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
@Schema(
  name = "ProjetoDTO",
  description = "Detalhes completos de um projeto incluindo informações básicas, datas e participantes",
  title = "Detalhes do Projeto",
  example = "{\"titulo\":\"Projeto de Extensão Exemplo\",\"descricao\":\"Descrição detalhada do projeto\",\"dataInicio\":\"2023-01-01\",\"dataFim\":\"2023-12-31\",\"status\":\"ATIVO\"}"
)
public class ProjetoDTO {

  private Long id;

  @NotNull @Size(min = 5, max = 100) private String titulo;

  @NotNull @Size(min = 20, max = 500) private String descricao;

  @NotNull @Size(min = 20, max = 500) private String justificativa;

  @NotNull private Date dataInicio;

  @NotNull private Date dataFim;

  @NotNull @Size(max = 500) private String publicoAlvo;

  @NotNull private boolean vinculadoDisciplina;

  @NotNull @Size(max = 500) private String restricaoPublico;

  @NotNull private List<Usuario> equipeExecutora;

  @NotNull private StatusProjeto status;
}
