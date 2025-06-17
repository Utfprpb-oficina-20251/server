package br.edu.utfpr.pb.ext.server.email;

import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
  @Email(message = "E-mail deve ter formato válido.") @NotBlank(message = "O e-mail é obrigatório.") private String email;

  // Código gerado
  @NotBlank(message = "O código é obrigatório.") @Column(nullable = false, unique = true)
  private String code;

  // Data e hora em que o código foi gerado
  @NotNull(message = "A data de geração é obrigatória.") @Column(name = "generated_at", nullable = false)
  private LocalDateTime generatedAt;

  // Se já foi utilizado
  @Column(nullable = false)
  private boolean used = false;

  // Data e hora de expiração
  @NotNull(message = "A data de expiração é obrigatória.") @Column(nullable = false)
  private LocalDateTime expiration;

  // Tipo do código: "cadastro", "recuperacao", etc.
  @NotNull(message = "O tipo do código é obrigatório.") @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TipoCodigo type;

  /**
   * Instancia um novo código de validação de e-mail com os dados fornecidos, marcando-o como não
   * utilizado.
   *
   * @param email endereço de e-mail do destinatário
   * @param code código de validação gerado
   * @param generatedAt data e hora de geração do código
   * @param expiration data e hora de expiração do código
   * @param type tipo do código de validação
   */
  public EmailCode(
      String email,
      String code,
      LocalDateTime generatedAt,
      LocalDateTime expiration,
      TipoCodigo type) {
    this.email = email;
    this.code = code;
    this.generatedAt = generatedAt;
    this.expiration = expiration;
    this.type = type;
    this.used = false;
  }
}
