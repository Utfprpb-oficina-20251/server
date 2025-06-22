package br.edu.utfpr.pb.ext.server.file;

import br.edu.utfpr.pb.ext.server.config.MinioConfig;
import br.edu.utfpr.pb.ext.server.file.exception.FileException;
import br.edu.utfpr.pb.ext.server.usuario.IUsuarioService;
import io.micrometer.core.annotation.Timed;
import io.minio.*;
import io.minio.messages.Item;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileService {
  public static final String ERRO_CARREGAMENTO_ARQUIVO_EXCEPTION = "Erro ao carregar o arquivo";
  public static final String ERRO_CARREGAR_ARQUIVO_LOG = "Erro ao carregar o arquivo: {}";
  public static final String ARQUIVO_VAZIO = "Arquivo vazio";
  private final MinioClient minioClient;
  private final MinioConfig minioConfig;
  private final IUsuarioService iusuarioService;
  private static final long MAX_MINIO_FILE_SIZE = 1024 * 1024 * 10L;
  private static final Set<String> ALLOWED_CONTENT_TYPES =
      Set.of(
          MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_PDF_VALUE);

  /**
   * Cria uma instância do serviço de arquivos utilizando o cliente MinIO, configurações e serviço
   * de usuário.
   *
   * @throws FileException se ocorrer um erro durante a inicialização do serviço.
   */
  public FileService(
      MinioClient minioClient, MinioConfig minioConfig, IUsuarioService iusuarioService)
      throws FileException {
    this.minioClient = minioClient;
    this.minioConfig = minioConfig;
    this.iusuarioService = iusuarioService;
  }

  /**
   * Armazena um arquivo enviado no armazenamento MinIO após validação.
   *
   * <p>Valida o arquivo quanto ao tamanho, tipo de conteúdo permitido e segurança do nome. Gera um
   * nome único para o arquivo, realiza o upload para o bucket configurado e retorna um objeto com
   * informações sobre o arquivo armazenado.
   *
   * @param file Arquivo a ser enviado.
   * @return Informações do arquivo armazenado, incluindo nome, tipo, tamanho, URL de acesso e data
   *     de upload.
   * @throws IllegalArgumentException se o arquivo estiver vazio ou inválido.
   * @throws FileException em caso de falha no armazenamento do arquivo.
   */
  @Timed(value = "file.upload", description = "Tempo de upload de arquivo")
  @PreAuthorize("isAuthenticated()")
  public FileInfoDTO store(MultipartFile file) {
    try {
      validateFile(file);
      String originalFilename =
          StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
      String filename = generateUniqueFilename(originalFilename);
      String contentType = file.getContentType();

      try (InputStream inputStream = file.getInputStream()) {
        minioClient.putObject(
            PutObjectArgs.builder().bucket(minioConfig.getBucket()).object(filename).stream(
                    inputStream, file.getSize(), -1)
                .contentType(contentType)
                .build());
      }

      String url = getUrl(filename);

      return new FileInfoDTO(
          filename, originalFilename, contentType, file.getSize(), url, LocalDateTime.now());
    } catch (IllegalArgumentException e) {
      log.error(ARQUIVO_VAZIO, e);
      throw new IllegalArgumentException(ARQUIVO_VAZIO, e);
    } catch (Exception e) {
      log.error("Erro ao armazenar o arquivo: {}", file.getOriginalFilename(), e);
      throw new FileException("Erro ao armazenar o arquivo", e);
    }
  }

  /**
   * Recupera um arquivo armazenado no MinIO como um recurso Spring.
   *
   * @param filename Nome do arquivo a ser recuperado.
   * @return Recurso contendo o conteúdo do arquivo solicitado.
   * @throws FileException Se o nome do arquivo for inválido ou ocorrer erro ao acessar o
   *     armazenamento.
   */
  @Timed(value = "file.download", description = "Tempo de download de arquivo")
  public Resource loadFileAsResource(String filename) {
    try {
      if (filename == null || filename.trim().isEmpty()) {
        log.warn("Nome nao pode ser nulo ou vazio");
        throw new FileException("Nome nao pode ser nulo ou vazio");
      }

      if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
        log.error("Tentativa de acessar arquivos fora do diretório atual: {}", filename);
        throw new FileException("Não é possível acessar arquivos fora do diretório atual");
      }

      GetObjectResponse response =
          minioClient.getObject(
              GetObjectArgs.builder().bucket(minioConfig.getBucket()).object(filename).build());

      byte[] content = response.readAllBytes();
      return new ByteArrayResource(content) {
        @Override
        public String getFilename() {
          return filename;
        }
      };
    } catch (Exception e) {
      log.error(ERRO_CARREGAR_ARQUIVO_LOG, filename, e);
      throw new FileException(ERRO_CARREGAMENTO_ARQUIVO_EXCEPTION);
    }
  }

  /**
   * Exclui um arquivo do bucket configurado no MinIO.
   *
   * @param filename nome do arquivo a ser excluído.
   * @throws FileException se o nome for nulo, vazio ou ocorrer erro durante a exclusão.
   */
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteFile(String filename) {
    log.info(
        "Deletando arquivo: {}, solicitado por {}",
        filename,
        iusuarioService.obterUsuarioLogado().getNome());
    try {
      if (filename == null || filename.trim().isEmpty()) {
        log.error("Nome não pode ser nulo ou vazio");
        throw new FileException("Nome nao pode ser nulo ou vazio");
      }
      minioClient.removeObject(
          RemoveObjectArgs.builder().bucket(minioConfig.getBucket()).object(filename).build());
      log.info("Arquivo deletado: {}", filename);
    } catch (Exception e) {
      log.error("Erro ao deletar o arquivo: {}", filename, e);
      throw new FileException("Erro ao deletar o arquivo", e);
    }
  }

  /**
   * Lista todos os arquivos presentes no bucket configurado do MinIO.
   *
   * @return Lista de objetos FileInfoDTO contendo metadados de cada arquivo, incluindo nome, tipo
   *     de conteúdo, tamanho, URL e data de modificação.
   * @throws FileException se ocorrer um erro ao acessar ou listar os arquivos no armazenamento.
   */
  public List<FileInfoDTO> listFiles() {
    List<FileInfoDTO> files = new ArrayList<>();
    try {
      Iterable<Result<Item>> results =
          minioClient.listObjects(
              ListObjectsArgs.builder().bucket(minioConfig.getBucket()).build());

      for (Result<Item> result : results) {
        Item item = result.get();
        files.add(
            new FileInfoDTO(
                item.objectName(),
                item.objectName(),
                getContentType(item.objectName()),
                item.size(),
                getUrl(item.objectName()),
                item.lastModified() != null
                    ? item.lastModified().toLocalDateTime()
                    : LocalDateTime.now()));
      }
      return files;
    } catch (Exception e) {
      log.error("Erro ao listar os arquivos", e);
      throw new FileException("Erro ao listar os arquivos", e);
    }
  }

  /**
   * Exclui um arquivo do armazenamento de forma assíncrona.
   *
   * <p>Requer que o usuário possua o papel de ADMIN.
   *
   * @param filename nome do arquivo a ser excluído.
   * @return um CompletableFuture concluído quando a exclusão for finalizada.
   */
  @Async
  @PreAuthorize("hasRole('ADMIN')")
  public CompletableFuture<Void> asyncDelete(String filename) {
    deleteFile(filename);
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Gera um nome de arquivo único baseado em UUID, preservando a extensão do arquivo original.
   *
   * @param originalFilename nome original do arquivo, incluindo a extensão.
   * @return nome de arquivo único com a mesma extensão do arquivo original.
   */
  private String generateUniqueFilename(String originalFilename) {
    String extension = "";
    int dotIndex = originalFilename.lastIndexOf('.');
    if (dotIndex > 0) {
      extension = originalFilename.substring(dotIndex);
    }
    return UUID.randomUUID() + extension;
  }

  /**
   * Obtém o content type de um arquivo armazenado no MinIO.
   *
   * <p>Caso o content type não esteja disponível ou ocorra um erro, retorna
   * "application/octet-stream".
   *
   * @param filename nome do arquivo no bucket do MinIO
   * @return o content type do arquivo ou "application/octet-stream" se indisponível
   */
  private String getContentType(String filename) {
    try {
      String contentType =
          minioClient
              .statObject(
                  StatObjectArgs.builder().bucket(minioConfig.getBucket()).object(filename).build())
              .contentType();
      return contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    } catch (Exception e) {
      log.warn("Erro ao obter o contentType do arquivo: {}", filename, e);
      return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
  }

  /**
   * Gera e retorna a URL de acesso ao arquivo armazenado no MinIO, com o nome do arquivo
   * devidamente codificado para uso em URLs.
   *
   * @param filename Nome do arquivo a ser acessado.
   * @return URL completa para acessar o arquivo no bucket configurado do MinIO.
   */
  @NotNull String getUrl(String filename) {
    String encodedFilename =
        URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
    return String.format(
        "%s/%s/%s", minioConfig.getUrl(), minioConfig.getBucket(), encodedFilename);
  }

  /**
   * Valida um arquivo enviado, garantindo que não seja nulo ou vazio, que o tipo de conteúdo seja
   * permitido, que o tamanho não exceda o limite máximo e que o nome não contenha sequências de
   * diretório inválidas.
   *
   * @param file Arquivo a ser validado.
   * @throws IllegalArgumentException se o arquivo for nulo ou vazio.
   * @throws FileException se o tipo de conteúdo não for permitido, o tamanho exceder o limite ou o
   *     nome for inválido.
   */
  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      log.error(ARQUIVO_VAZIO);
      throw new IllegalArgumentException(ARQUIVO_VAZIO);
    }

    if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
      log.error("Tipo de conteudo nao permitido: {}", file.getContentType());
      throw new FileException("Tipo de conteudo nao permitido");
    }

    if (file.getSize() > MAX_MINIO_FILE_SIZE) {
      log.error(
          "tamanho do arquivo: {} excede o limite permitido: {}",
          formatFileSize(file.getSize()),
          formatFileSize(MAX_MINIO_FILE_SIZE));
      throw new FileException("Tamanho do arquivo excede o limite permitido");
    }
    String originalFilename =
        StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
    if (originalFilename.contains("..")) {
      log.error("Tentativa de armazenar um arquivo fora do diretório atual: {}", originalFilename);
      throw new FileException("Nome de arquivo inválido");
    }
  }

  /**
   * Converte o tamanho em bytes para uma string legível com unidade apropriada (B, kB, MB, etc.).
   *
   * @param bytes quantidade de bytes a ser formatada
   * @return representação legível do tamanho do arquivo com unidade
   */
  private String formatFileSize(long bytes) {
    if (bytes < 1024) {
      return String.format("%d B", bytes);
    }
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    return String.format("%.1f %cB", bytes / Math.pow(1024, exp), "kMGTPE".charAt(exp));
  }
}
