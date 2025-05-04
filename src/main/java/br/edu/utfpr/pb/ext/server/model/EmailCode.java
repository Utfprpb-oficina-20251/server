package br.edu.utfpr.pb.ext.server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Entidade que representa um código temporário enviado por e-mail, usado para validação em
 * processos como cadastro ou recuperação de senha.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_access_code")
public class EmailCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // E-mail destinatário do código
  @Column(nullable = false)
  private String email;

  // Código gerado
  @Column(nullable = false, unique = true)
  private String code;

  // Data e hora em que o código foi gerado
  @Column(name = "generated_at", nullable = false)
  private LocalDateTime generatedAt;

  // Se já foi utilizado
  @Column(nullable = false)
  private boolean used = false;

  // Data e hora de expiração
  @Column(nullable = false)
  private LocalDateTime expiration;

  // Tipo do código: "cadastro", "recuperacao", etc.
  @Column(nullable = false)
  private String type;
}
