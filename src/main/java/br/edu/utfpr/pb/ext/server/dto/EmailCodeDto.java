package br.edu.utfpr.pb.ext.server.dto;

import java.time.LocalDateTime;
import lombok.Data;

/** DTO para transferência de dados de EmailCode. */
@Data
public class EmailCodeDto {

  private Long id;
  private String email;
  private String code;
  private String type;
  private Boolean used;
  private LocalDateTime generatedAt;
  private LocalDateTime expiration;
}
