package br.edu.utfpr.pb.ext.server.notificacao;

import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoNotificacao;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoReferencia;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tb_notificacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notificacao extends BaseEntity {
  @Column(nullable = false, length = 100, name = "titulo")
  private String titulo;

  @Column(nullable = false, columnDefinition = "TEXT", name = "descricao")
  private String descricao;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "tipo_notificacao")
  private TipoNotificacao tipoNotificacao;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_referencia")
  private TipoReferencia tipoReferencia;

  @Column(name = "referencia_id")
  private Long referenciaId;

  @Column(name = "data_criacao", nullable = false)
  private LocalDateTime dataCriacao;

  @Column(name = "lida")
  @Builder.Default
  private boolean lida = false;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  public void marcarComoLida() {
    this.lida = true;
  }

  public boolean pertenceAoUsuario(Usuario usuario) {
    return this.usuario != null && this.usuario.getId().equals(usuario.getId());
  }
}
