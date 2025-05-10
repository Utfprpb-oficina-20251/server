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
   * Fornece um bean do ModelMapper para mapeamento automático de objetos entre diferentes tipos.
   *
   * @return uma instância de ModelMapper para uso em injeção de dependências
   */
  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

  /**
   * Adiciona o prefixo "/api/" a todas as rotas de controladores que implementam ou estendem {@code
   * CrudController}.
   *
   * <p>Isso centraliza o versionamento e a organização dos endpoints de CRUD sob o namespace
   * "/api/".
   *
   * @param configurer objeto utilizado para configurar o mapeamento de caminhos no Spring MVC
   */
  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix("/api/", HandlerTypePredicate.forAssignableType(CrudController.class));
  }
}
