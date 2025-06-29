package br.edu.utfpr.pb.ext.server.candidatura;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CandidaturaDTO {
  private Long id;
  private Long projetoId;
  private LocalDateTime dataCandidatura;
}
