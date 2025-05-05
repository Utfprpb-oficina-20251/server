package br.edu.utfpr.pb.ext.server.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {
  @Value("${spring.sendgrid.api-key}")
  private String apiKey;

  /**
   * Cria e fornece um bean SendGrid configurado com a chave de API definida nas propriedades da aplicação.
   *
   * @return uma instância de SendGrid pronta para envio de e-mails
   */
  @Bean
  public SendGrid sendGrid() {
    return new SendGrid(apiKey);
  }
}
