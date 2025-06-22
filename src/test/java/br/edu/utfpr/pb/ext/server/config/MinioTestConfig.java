package br.edu.utfpr.pb.ext.server.config;

import io.minio.MinioClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MinioTestConfig {
  @Bean
  public MinioClient minioClient() {
    return Mockito.mock(MinioClient.class);
  }
}
