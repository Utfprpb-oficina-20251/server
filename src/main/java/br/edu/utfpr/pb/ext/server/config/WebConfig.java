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
  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix("/api/", HandlerTypePredicate.forAssignableType(CrudController.class));
  }
}
