package br.edu.utfpr.pb.ext.server.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@Getter
public class MinioConfig {
  @Value("${minio.url}")
  private String url;

  @Value("${minio.access-key}")
  private String accessKey;

  @Value("${minio.secret-key}")
  private String secretKey;

  @Value("${minio.bucket}")
  private String bucket;

  @Value("${minio.secure}")
  private boolean secure;

  @Bean
  public io.minio.MinioClient minioClient() {
    if (!StringUtils.hasText(url)) {
      throw new IllegalArgumentException("Url do Minio deve ser informada.");
    }
    if (!StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey)) {
      throw new IllegalArgumentException("Minio access key is required");
    }

    return io.minio.MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
  }
}
