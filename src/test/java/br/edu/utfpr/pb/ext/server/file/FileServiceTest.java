package br.edu.utfpr.pb.ext.server.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.config.MinioConfig;
import br.edu.utfpr.pb.ext.server.file.exception.FileException;
import br.edu.utfpr.pb.ext.server.usuario.IUsuarioService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import io.minio.*;
import io.minio.messages.Item;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {
  public static final String TEST_BASE_URL = "http://localhost:9000";
  @Mock private MinioClient minioClient;

  @Mock private MinioConfig minioConfig;

  @Mock private IUsuarioService usuarioService;

  private FileService fileService;

  private static final String BUCKET_NAME = "test-bucket";
  private static final String TEST_FILENAME = "test.jpg";
  private static final String TEST_CONTENT = "test content";
  private static final byte[] TEST_BYTES = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);

  @BeforeEach
  void setUp() {
    fileService = new FileService(minioClient, minioConfig, usuarioService);
  }

  @Test
  void store_ValidFile_ReturnsFileInfo() throws Exception {
    // Arrange
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    MultipartFile file =
        new MockMultipartFile(TEST_FILENAME, TEST_FILENAME, MediaType.IMAGE_JPEG_VALUE, TEST_BYTES);
    when(minioConfig.getUrl()).thenReturn(TEST_BASE_URL);

    ObjectWriteResponse response = mock(ObjectWriteResponse.class);
    when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(response);

    // Act
    FileInfoDTO result = fileService.store(file);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_FILENAME, result.getOriginalFileName());
    assertTrue(result.getUrl().contains(result.getFileName()));
    verify(minioClient).putObject(any(PutObjectArgs.class));
  }

  @Test
  void store_EmptyFile_ThrowsException() {
    // Arrange
    MultipartFile emptyFile = new MockMultipartFile("empty.jpg", new byte[0]);

    // Act & Assert
    RuntimeException ex =
        assertThrows(IllegalArgumentException.class, () -> fileService.store(emptyFile));
    assertEquals(FileService.ARQUIVO_VAZIO, ex.getMessage());
  }

  @Test
  void loadFileAsResource_ValidFilename_ReturnsResource() throws Exception {
    // Arrange
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    GetObjectResponse response = mock(GetObjectResponse.class);
    when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);
    when(response.readAllBytes()).thenReturn(TEST_BYTES);

    // Act
    Resource resource = fileService.loadFileAsResource(TEST_FILENAME);

    // Assert
    assertNotNull(resource);
    assertArrayEquals(TEST_BYTES, resource.getInputStream().readAllBytes());
    assertEquals(TEST_FILENAME, resource.getFilename());
    verify(minioClient)
        .getObject(
            argThat(
                args -> args.bucket().equals(BUCKET_NAME) && args.object().equals(TEST_FILENAME)));
  }

  @Test
  void loadFileAsResource_InvalidFilename_ThrowsException() {
    // Act & Assert
    assertThrows(FileException.class, () -> fileService.loadFileAsResource("../invalid.jpg"));
  }

  @Test
  void deleteFile_ValidFilename_DeletesFile() throws Exception {
    // Arrange
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    when(usuarioService.obterUsuarioLogado())
        .thenReturn(Usuario.builder().nome("Test User").build());
    doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

    // Act
    fileService.deleteFile(TEST_FILENAME);

    // Assert
    verify(minioClient).removeObject(any(RemoveObjectArgs.class));
  }

  @Test
  void listFiles_ReturnsFileList() {
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    when(minioConfig.getUrl()).thenReturn(TEST_BASE_URL);
    // Arrange
    Item item = mock(Item.class);
    when(item.objectName()).thenReturn(TEST_FILENAME);
    when(item.size()).thenReturn(1024L);
    Result<Item> result = new Result<>(item);
    when(minioClient.listObjects(any(ListObjectsArgs.class)))
        .thenReturn(new MockResultIterator<>(result));

    // Act
    List<FileInfoDTO> files = fileService.listFiles();

    // Assert
    assertFalse(files.isEmpty());
    assertEquals(TEST_FILENAME, files.getFirst().getFileName());
  }

  @Test
  void asyncDelete_ValidFilename_CompletesSuccessfully() throws Exception {
    // Arrange
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    when(usuarioService.obterUsuarioLogado())
        .thenReturn(Usuario.builder().nome("Test User").build());
    doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

    // Act
    CompletableFuture<Void> future = fileService.asyncDelete(TEST_FILENAME);

    // Assert
    future.get(); // Wait for completion
    verify(minioClient).removeObject(any(RemoveObjectArgs.class));
  }

  @Test
  void validateFile_InvalidContentType_ThrowsException() {
    // Arrange
    MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", TEST_BYTES);

    // Act & Assert
    assertThrows(FileException.class, () -> fileService.store(file));
  }

  @Test
  void validateFile_FileTooLarge_ThrowsException() {
    // Arrange
    byte[] largeFile = new byte[11 * 1024 * 1024]; // 11MB
    MultipartFile file =
        new MockMultipartFile("large.jpg", "large.jpg", MediaType.IMAGE_JPEG_VALUE, largeFile);

    // Act & Assert
    assertThrows(FileException.class, () -> fileService.store(file));
  }

  @Test
  void getUrl_ValidFilename_ReturnsEncodedUrl() {
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    when(minioConfig.getUrl()).thenReturn(TEST_BASE_URL);
    // Arrange
    String filename = "test file.jpg";
    // Act
    String url = fileService.getUrl(filename);

    // Assert
    assertTrue(url.contains("test%20file.jpg"));
    assertTrue(url.startsWith("http://localhost:9000/test-bucket/"));
  }

  // Helper class for mocking MinIO results
  private record MockResultIterator<T>(Result<T> result) implements Iterable<Result<T>> {

    @NotNull @Override
    public java.util.Iterator<Result<T>> iterator() {
      return new java.util.Iterator<>() {
        private boolean hasNext = true;

        @Override
        public boolean hasNext() {
          return hasNext;
        }

        @Override
        public Result<T> next() {
          if (!hasNext) {
            throw new IllegalStateException("No more elements");
          }
          hasNext = false;
          return result;
        }
      };
    }
  }
}
