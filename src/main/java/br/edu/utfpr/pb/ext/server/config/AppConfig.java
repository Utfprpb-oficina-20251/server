package br.edu.utfpr.pb.ext.server.config;

import br.edu.utfpr.pb.ext.server.generics.CrudController;
import java.lang.reflect.Method;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class AppConfig {
  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

  @Bean
  public WebMvcRegistrations webMvcRegistrations() {
    return new WebMvcRegistrations() {
      @Override
      public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping() {
          @Override
          protected void registerHandlerMethod(
              Object handler, Method method, RequestMappingInfo mapping) {
            if (handler instanceof CrudController) {
              mapping =
                  new RequestMappingInfo(
                      mapping.getName(),
                      new PatternsRequestCondition("/api").combine(mapping.getPatternsCondition()),
                      mapping.getMethodsCondition(),
                      mapping.getParamsCondition(),
                      mapping.getHeadersCondition(),
                      mapping.getConsumesCondition(),
                      mapping.getProducesCondition(),
                      mapping.getCustomCondition());
            }
            super.registerHandlerMethod(handler, method, mapping);
          }
        };
      }
    };
  }
}
