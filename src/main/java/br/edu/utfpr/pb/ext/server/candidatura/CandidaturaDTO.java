package br.edu.utfpr.pb.ext.server.candidatura;

import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioProjetoDTO;
import java.time.LocalDateTime;
import lombok.*;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CandidaturaDTO {
  private Long id;
  private UsuarioProjetoDTO usuarioProjeto;
  private Long projetoId;
  private StatusCandidatura statusCandidatura;
  private LocalDateTime dataCandidatura;
}
