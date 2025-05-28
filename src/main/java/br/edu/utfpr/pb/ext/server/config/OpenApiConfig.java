package br.edu.utfpr.pb.ext.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  /**
   * Configura e retorna uma instância do OpenAPI com suporte à autenticação JWT via Bearer Token.
   *
   * <p>Adiciona um requisito de segurança chamado "bearerAuth" e define o esquema de segurança
   * correspondente como HTTP Bearer com formato JWT na documentação OpenAPI da aplicação.
   *
   * @return instância configurada de OpenAPI com autenticação JWT Bearer.
   */
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
