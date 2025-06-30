package br.edu.utfpr.pb.ext.server.notificacao;

import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
  Page<Notificacao> findByUsuarioIdOrderByDataCriacaoDesc(Usuario usuario, Pageable pageable);

  Page<Notificacao> findByUsuarioIdAndLidaFalseOrderByDataCriacaoDesc(
      Usuario usuario, Pageable pageable);

  long countByUsuarioAndLidaFalse(Usuario usuario);

  @Modifying
  @Query("UPDATE Notificacao n SET n.lida = true WHERE n.usuario = :usuario AND n.lida = false")
  void marcarTodasComoLidas(@Param("usuario") Usuario usuario);
}
