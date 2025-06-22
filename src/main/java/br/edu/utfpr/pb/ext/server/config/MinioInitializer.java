package br.edu.utfpr.pb.ext.server.config;

import br.edu.utfpr.pb.ext.server.file.exception.FileException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MinioInitializer {
  private final MinioClient minioClient;
  private final MinioConfig minioConfig;

  /**
   * Inicializa o bucket do MinIO ao iniciar a aplicação.
   *
   * Verifica se o bucket especificado existe; caso não exista, cria o bucket e define uma política de acesso público de leitura para todos os objetos nele contidos.
   * Em caso de erro durante o processo, lança uma FileException encapsulando a exceção original.
   */
  @PostConstruct
  public void initializeBucket() {
    try {
      if (!minioClient.bucketExists(
          BucketExistsArgs.builder().bucket(minioConfig.getBucket()).build())) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConfig.getBucket()).build());
        log.info("Created bucket: {}", minioConfig.getBucket());
      }

      String policyJson =
          String.format(
              """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """,
              minioConfig.getBucket());

      minioClient.setBucketPolicy(
          SetBucketPolicyArgs.builder().bucket(minioConfig.getBucket()).config(policyJson).build());

    } catch (Exception e) {
      log.error("Error initializing MinIO bucket: {}", minioConfig.getBucket(), e);
      throw new FileException("Failed to initialize MinIO bucket", e);
    }
  }
}
