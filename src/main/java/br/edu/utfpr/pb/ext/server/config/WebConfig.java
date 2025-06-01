package br.edu.utfpr.pb.ext.server.config;

import br.edu.utfpr.pb.ext.server.generics.CrudController;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  /**
   * Cria e fornece um bean ModelMapper para mapeamento automático de objetos entre tipos distintos.
   *
   * @return uma instância de ModelMapper disponível para injeção de dependências
   */
  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

  /**
   * Configura o prefixo "/api/" para todas as rotas de controladores que implementam ou estendem {@code CrudController}.
   *
   * Centraliza os endpoints de operações CRUD sob o namespace "/api/", facilitando a organização e o roteamento das APIs.
   *
   * @param configurer objeto utilizado para definir regras de mapeamento de caminhos no Spring MVC
   */
  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix("/api/", HandlerTypePredicate.forAssignableType(CrudController.class));
  }
}
