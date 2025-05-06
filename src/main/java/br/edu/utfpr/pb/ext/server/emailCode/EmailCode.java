package br.edu.utfpr.pb.ext.server.emailCode;

import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade que representa um código temporário enviado por e-mail, usado para validação em
 * processos como cadastro ou recuperação de senha.
 */
@Entity
@Table(name = "tb_access_code")
@Getter
@Setter
@NoArgsConstructor
public class EmailCode extends BaseEntity {

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

  // Construtor auxiliar para testes ou uso direto
  public EmailCode(
      String email, String code, LocalDateTime generatedAt, LocalDateTime expiration, String type) {
    this.email = email;
    this.code = code;
    this.generatedAt = generatedAt;
    this.expiration = expiration;
    this.type = type;
    this.used = false;
  }
}
