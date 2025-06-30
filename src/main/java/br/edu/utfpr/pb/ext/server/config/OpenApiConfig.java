package br.edu.utfpr.pb.ext.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  /**
   * Cria e configura uma instância do OpenAPI com suporte à autenticação JWT via Bearer Token.
   *
   * <p>Adiciona um requisito de segurança "bearerAuth" e define o esquema de segurança HTTP Bearer
   * com formato JWT na documentação OpenAPI da aplicação.
   *
   * @return instância do OpenAPI configurada para autenticação JWT Bearer
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

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .pathsToMatch("/api/**")
        .addOperationCustomizer(
            (operation, handlerMethod) -> {
              // Customiza parâmetros do Pageable
              if (operation.getParameters() != null) {
                operation
                    .getParameters()
                    .forEach(
                        parameter -> {
                          if ("page".equals(parameter.getName())) {
                            parameter.setDescription("Número da página (inicia em 0)");
                          } else if ("size".equals(parameter.getName())) {
                            parameter.setDescription("Quantidade de itens por página");
                          } else if ("sort".equals(parameter.getName())) {
                            parameter.setDescription(
                                "Ordenação no formato: propriedade,direção (ex: 'dataCriacao,desc')");
                          }
                        });
              }
              return operation;
            })
        .build();
  }
}
