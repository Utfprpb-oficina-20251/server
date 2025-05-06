package br.edu.utfpr.pb.ext.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** DTO para transferência de dados da entidade EmailCode. */
public class EmailCodeDto {

  private Long id;

  @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") private String email;

  @NotBlank(message = "O código é obrigatório.") private String code;

  @NotBlank(message = "O tipo do código é obrigatório.") private String type;

  @NotNull(message = "O status de uso é obrigatório.") private Boolean used;

  @NotNull(message = "A data de geração é obrigatória.") private LocalDateTime generatedAt;

  @NotNull(message = "A data de expiração é obrigatória.") private LocalDateTime expiration;

  // Getters e Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Boolean getUsed() {
    return used;
  }

  public void setUsed(Boolean used) {
    this.used = used;
  }

  public LocalDateTime getGeneratedAt() {
    return generatedAt;
  }

  public void setGeneratedAt(LocalDateTime generatedAt) {
    this.generatedAt = generatedAt;
  }

  public LocalDateTime getExpiration() {
    return expiration;
  }

  public void setExpiration(LocalDateTime expiration) {
    this.expiration = expiration;
  }
}
