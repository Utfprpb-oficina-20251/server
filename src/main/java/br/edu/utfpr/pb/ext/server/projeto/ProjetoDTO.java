package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioProjetoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
@Schema(name = "ProjetoDTO", description = "DTO para detalhamento de projeto")
public class ProjetoDTO {

  private Long id;

  @NotNull @Size(min = 5, max = 100) private String titulo;

  @NotNull @Size(min = 20, max = 500) private String descricao;

  @NotNull @Size(min = 20, max = 500) private String justificativa;

  @NotNull @JsonFormat(pattern = "yyyy-MM-dd")
  private Date dataInicio;

  @NotNull @JsonFormat(pattern = "yyyy-MM-dd")
  private Date dataFim;

  @NotNull @Size(max = 500) private String publicoAlvo;

  @NotNull private boolean vinculadoDisciplina;

  @NotNull @Size(max = 500) private String restricaoPublico;

  @NotNull private List<UsuarioProjetoDTO> equipeExecutora;

  private StatusProjeto status;

  private Long cargaHoraria;

  private Long qtdeVagas;

  @Schema(description = "Usuário responsável pelo projeto")
  private UsuarioProjetoDTO responsavel;

  @Schema(
      description = "Recebe Base64 DataURI ou URL Externa. Projetos salvos só retornam url",
      example =
          "https://example.com/image.jpg OU data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/wcAAwAB/epv2AAAAABJRU5ErkJggg==")
  private String imagemUrl;
}
