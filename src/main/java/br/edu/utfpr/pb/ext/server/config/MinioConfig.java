package br.edu.utfpr.pb.ext.server.config;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

@Configuration
@Getter
@Slf4j
public class MinioConfig {
  @Value("${minio.url}")
  private String url;

  @Value("${minio.access-key}")
  private String accessKey;

  @Value("${minio.secret-key}")
  private String secretKey;

  @Value("${minio.bucket}")
  private String bucket;

  /**
   * Cria e fornece um bean {@link MinioClient} configurado com as propriedades definidas para o
   * Minio.
   *
   * @return uma instância configurada de {@link MinioClient}
   * @throws IllegalArgumentException se a URL, a access key ou a secret key do Minio não estiverem
   *     definidas ou estiverem vazias
   */
  @Bean
  public MinioClient minioClient() {
    if (!StringUtils.hasText(url)) {
      throw new IllegalArgumentException("Url do Minio deve ser informada.");
    }
    if (!StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey)) {
      throw new IllegalArgumentException("Credenciais do Minio devem ser informadas.");
    }

    return MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
  }

  /**
   * Cria e registra um bean MinioInitializer para inicializar recursos do Minio, exceto no perfil
   * "test".
   *
   * @param minioClient instância configurada do MinioClient.
   * @return instância de MinioInitializer configurada com o MinioClient e esta configuração.
   */
  @Bean
  @Profile("!test")
  public MinioInitializer minioInitializer(MinioClient minioClient) {
    return new MinioInitializer(minioClient, this);
  }
}
