package br.edu.utfpr.pb.ext.server.config;

import io.minio.MinioClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MinioTestConfig {
  /**
   * Fornece um bean de teste que retorna uma instância mockada de MinioClient.
   *
   * <p>Permite a injeção de um MinioClient simulado em testes, evitando a necessidade de conexão
   * com um servidor Minio real.
   *
   * @return instância mockada de MinioClient para uso em testes
   */
  @Bean
  public MinioClient minioClient() {
    return Mockito.mock(MinioClient.class);
  }
}
