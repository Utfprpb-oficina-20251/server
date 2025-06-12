package br.edu.utfpr.pb.ext.server.email;

import br.edu.utfpr.pb.ext.server.email.enums.TipoCodigo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** DTO para transferência de dados da entidade EmailCode. */
public class EmailCodeDto {

  private Long id;

  @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") private String email;

  @NotBlank(message = "O código é obrigatório.") private String code;

  @NotBlank(message = "O tipo do código é obrigatório.") private TipoCodigo type;

  @NotNull(message = "O status de uso é obrigatório.") private Boolean used;

  @NotNull(message = "A data de geração é obrigatória.") private LocalDateTime generatedAt;

  @NotNull(message = "A data de expiração é obrigatória.") private LocalDateTime expiration;

  /**
   * Retorna o identificador único do código de e-mail.
   *
   * @return o ID do código de e-mail
   */
  public Long getId() {
    return id;
  }

  /**
   * Define o identificador do código de e-mail.
   *
   * @param id valor a ser atribuído ao identificador
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Retorna o endereço de e-mail associado ao código.
   *
   * @return o e-mail vinculado ao código
   */
  public String getEmail() {
    return email;
  }

  /**
   * Define o endereço de e-mail associado ao código.
   *
   * @param email endereço de e-mail a ser definido
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Retorna o código associado ao e-mail.
   *
   * @return o código como uma string
   */
  public String getCode() {
    return code;
  }

  /**
   * Define o valor do código associado ao e-mail.
   *
   * @param code código a ser atribuído
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * Retorna o tipo ou categoria associado ao código de e-mail.
   *
   * @return o tipo do código
   */
  public TipoCodigo getType() {
    return type;
  }

  /**
   * Define o tipo ou categoria do código de e-mail.
   *
   * @param type tipo do código
   */
  public void setType(TipoCodigo type) {
    this.type = type;
  }

  /**
   * Retorna se o código já foi utilizado.
   *
   * @return true se o código foi utilizado, false caso contrário
   */
  public Boolean getUsed() {
    return used;
  }

  /**
   * Define se o código foi utilizado.
   *
   * @param used valor booleano indicando se o código já foi utilizado
   */
  public void setUsed(Boolean used) {
    this.used = used;
  }

  /**
   * Retorna a data e hora em que o código foi gerado.
   *
   * @return o timestamp de geração do código
   */
  public LocalDateTime getGeneratedAt() {
    return generatedAt;
  }

  /**
   * Define a data e hora em que o código foi gerado.
   *
   * @param generatedAt data e hora de geração do código
   */
  public void setGeneratedAt(LocalDateTime generatedAt) {
    this.generatedAt = generatedAt;
  }

  /**
   * Retorna a data e hora de expiração do código de e-mail.
   *
   * @return data e hora em que o código expira
   */
  public LocalDateTime getExpiration() {
    return expiration;
  }

  /**
   * Define a data e hora de expiração do código.
   *
   * @param expiration data e hora em que o código expira
   */
  public void setExpiration(LocalDateTime expiration) {
    this.expiration = expiration;
  }
}
