package br.edu.utfpr.pb.ext.server.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
  /**
   * Fornece um bean ModelMapper para o contexto da aplicação Spring.
   *
   * @return uma nova instância de ModelMapper
   */
  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }
}
