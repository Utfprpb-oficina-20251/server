package br.edu.utfpr.pb.ext.server.file;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.config.MinioConfig;
import br.edu.utfpr.pb.ext.server.file.exception.FileException;
import br.edu.utfpr.pb.ext.server.usuario.IUsuarioService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
  void storeBytes_ValidData_ReturnsFileInfo() throws Exception {
    // Arrange
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    when(minioConfig.getUrl()).thenReturn(TEST_BASE_URL);

    String contentType = MediaType.IMAGE_JPEG_VALUE;
    String originalFilename = "test-image.jpg";

    ObjectWriteResponse response = mock(ObjectWriteResponse.class);
    when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(response);

    // Act
    FileInfoDTO result = fileService.store(TEST_BYTES, contentType, originalFilename);

    // Assert
    assertNotNull(result);
    assertEquals(contentType, result.getContentType());
    assertEquals(TEST_BYTES.length, result.getSize());
    assertTrue(result.getUrl().contains(result.getFileName()));
    assertTrue(result.getUrl().startsWith(TEST_BASE_URL));
    assertNotNull(result.getUploadDate());
    verify(minioClient).putObject(any(PutObjectArgs.class));
  }

  @Test
  void storeBytes_NullData_ThrowsIllegalArgumentException()
      throws ServerException,
          InsufficientDataException,
          ErrorResponseException,
          IOException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          InvalidResponseException,
          XmlParserException,
          InternalException {
    // Arrange
    String contentType = MediaType.IMAGE_JPEG_VALUE;
    String originalFilename = "test-image.jpg";

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> fileService.store(null, contentType, originalFilename));

    assertEquals(FileService.ARQUIVO_VAZIO, exception.getMessage());
    verify(minioClient, never()).putObject(any(PutObjectArgs.class));
  }

  @Test
  void storeBytes_EmptyData_ThrowsIllegalArgumentException()
      throws ServerException,
          InsufficientDataException,
          ErrorResponseException,
          IOException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          InvalidResponseException,
          XmlParserException,
          InternalException {
    // Arrange
    byte[] emptyData = new byte[0];
    String contentType = MediaType.IMAGE_JPEG_VALUE;
    String originalFilename = "test-image.jpg";

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> fileService.store(emptyData, contentType, originalFilename));

    assertEquals(FileService.ARQUIVO_VAZIO, exception.getMessage());
    verify(minioClient, never()).putObject(any(PutObjectArgs.class));
  }

  @Test
  void storeBytes_DataTooLarge_ThrowsFileException()
      throws ServerException,
          InsufficientDataException,
          ErrorResponseException,
          IOException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          InvalidResponseException,
          XmlParserException,
          InternalException {
    // Arrange
    byte[] largeData = new byte[11 * 1024 * 1024]; // 11MB, exceeding the 10MB limit
    String contentType = MediaType.IMAGE_JPEG_VALUE;
    String originalFilename = "large-image.jpg";

    // Act & Assert
    FileException exception =
        assertThrows(
            FileException.class, () -> fileService.store(largeData, contentType, originalFilename));

    assertTrue(exception.getMessage().contains("O arquivo excede o tamanho máximo"));
    verify(minioClient, never()).putObject(any(PutObjectArgs.class));
  }

  @Test
  void storeBytes_InvalidContentType_ThrowsFileException()
      throws ServerException,
          InsufficientDataException,
          ErrorResponseException,
          IOException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          InvalidResponseException,
          XmlParserException,
          InternalException {
    // Arrange
    String invalidContentType = "text/plain";
    String originalFilename = "test.txt";

    // Act & Assert
    FileException exception =
        assertThrows(
            FileException.class,
            () -> fileService.store(TEST_BYTES, invalidContentType, originalFilename));

    assertEquals("Tipo de arquivo não permitido: " + invalidContentType, exception.getMessage());
    verify(minioClient, never()).putObject(any(PutObjectArgs.class));
  }

  @Test
  void storeBytes_ValidPngImage_ReturnsFileInfo() throws Exception {
    // Arrange
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    when(minioConfig.getUrl()).thenReturn(TEST_BASE_URL);

    String contentType = MediaType.IMAGE_PNG_VALUE;
    String originalFilename = "test-image.png";

    ObjectWriteResponse response = mock(ObjectWriteResponse.class);
    when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(response);

    // Act
    FileInfoDTO result = fileService.store(TEST_BYTES, contentType, originalFilename);

    // Assert
    assertNotNull(result);
    assertEquals(contentType, result.getContentType());
    assertEquals(TEST_BYTES.length, result.getSize());
    assertTrue(result.getUrl().contains(result.getFileName()));
    verify(minioClient).putObject(any(PutObjectArgs.class));
  }

  @Test
  void storeBytes_ValidPdfFile_ReturnsFileInfo() throws Exception {
    // Arrange
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    when(minioConfig.getUrl()).thenReturn(TEST_BASE_URL);

    String contentType = MediaType.APPLICATION_PDF_VALUE;
    String originalFilename = "document.pdf";

    ObjectWriteResponse response = mock(ObjectWriteResponse.class);
    when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(response);

    // Act
    FileInfoDTO result = fileService.store(TEST_BYTES, contentType, originalFilename);

    // Assert
    assertNotNull(result);
    assertEquals(contentType, result.getContentType());
    assertEquals(TEST_BYTES.length, result.getSize());
    assertTrue(result.getUrl().contains(result.getFileName()));
    verify(minioClient).putObject(any(PutObjectArgs.class));
  }

  @Test
  void storeBytes_MinioException_ThrowsFileException() throws Exception {
    // Arrange
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);

    String contentType = MediaType.IMAGE_JPEG_VALUE;
    String originalFilename = "test-image.jpg";

    when(minioClient.putObject(any(PutObjectArgs.class)))
        .thenThrow(new RuntimeException("MinIO connection error"));

    // Act & Assert
    FileException exception =
        assertThrows(
            FileException.class,
            () -> fileService.store(TEST_BYTES, contentType, originalFilename));

    assertEquals(FileService.ERRO_CARREGAMENTO_ARQUIVO_EXCEPTION, exception.getMessage());
    verify(minioClient).putObject(any(PutObjectArgs.class));
  }

  @Test
  void storeBytes_GeneratesUniqueFilename() throws Exception {
    // Arrange
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    when(minioConfig.getUrl()).thenReturn(TEST_BASE_URL);

    String contentType = MediaType.IMAGE_JPEG_VALUE;
    String originalFilename = "test-image.jpg";

    ObjectWriteResponse response = mock(ObjectWriteResponse.class);
    when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(response);

    // Act
    FileInfoDTO result1 = fileService.store(TEST_BYTES, contentType, originalFilename);
    FileInfoDTO result2 = fileService.store(TEST_BYTES, contentType, originalFilename);

    // Assert
    assertNotNull(result1.getFileName());
    await().atLeast(Duration.ofMillis(1));
    assertNotNull(result2.getFileName());
    assertNotEquals(result1.getFileName(), result2.getFileName());
    assertTrue(result1.getFileName().contains(".jpg"));
    assertTrue(result2.getFileName().contains(".jpg"));
  }

  @Test
  void storeBytes_GeneratesCorrectStructure() throws Exception {
    // Arrange
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    when(minioConfig.getUrl()).thenReturn(TEST_BASE_URL);

    String contentType = MediaType.IMAGE_JPEG_VALUE;
    String originalFilename = "test-image.jpg";

    ObjectWriteResponse response = mock(ObjectWriteResponse.class);
    when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(response);

    // Act
    FileInfoDTO result = fileService.store(TEST_BYTES, contentType, originalFilename);

    // Assert
    assertNotNull(result.getUrl());
    // The URL should contain the encoded filename, but since generateUniqueFilename creates a UUID,
    // we just verify the URL structure
    assertTrue(result.getUrl().startsWith(TEST_BASE_URL + "/" + BUCKET_NAME + "/"));
    assertTrue(result.getUrl().contains(".jpg"));
    assertTrue(result.getUrl().matches("^https?://.*"));
  }

  @Test
  void storeBytes_SetsCorrectUploadDate() throws Exception {
    // Arrange
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    when(minioConfig.getUrl()).thenReturn(TEST_BASE_URL);

    String contentType = MediaType.IMAGE_JPEG_VALUE;
    String originalFilename = "test-image.jpg";

    ObjectWriteResponse response = mock(ObjectWriteResponse.class);
    when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(response);

    LocalDateTime beforeUpload = LocalDateTime.now().minusSeconds(1);

    // Act
    FileInfoDTO result = fileService.store(TEST_BYTES, contentType, originalFilename);

    LocalDateTime afterUpload = LocalDateTime.now().plusSeconds(1);

    // Assert
    assertNotNull(result.getUploadDate());
    assertTrue(result.getUploadDate().isAfter(beforeUpload));
    assertTrue(result.getUploadDate().isBefore(afterUpload));
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
  void listFiles_WithPagination_ReturnsPaginatedFileList() {
    // Arrange
    when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    when(minioConfig.getUrl()).thenReturn(TEST_BASE_URL);
    ZonedDateTime now = ZonedDateTime.now();

    Item item1 = mock(Item.class);
    when(item1.objectName()).thenReturn("file1.jpg");
    when(item1.size()).thenReturn(1024L);
    when(item1.lastModified()).thenReturn(now);

    Item item2 = mock(Item.class);
    when(item2.objectName()).thenReturn("file2.jpg");
    when(item2.size()).thenReturn(2048L);
    when(item2.lastModified()).thenReturn(now);

    Item item3 = mock(Item.class);
    when(item3.objectName()).thenReturn("file3.jpg");
    when(item3.size()).thenReturn(3072L);
    when(item3.lastModified()).thenReturn(now);

    List<Result<Item>> results =
        List.of(new Result<>(item1), new Result<>(item2), new Result<>(item3));
    when(minioClient.listObjects(any(ListObjectsArgs.class))).thenAnswer(invocation -> results);
    Pageable pageable = PageRequest.of(0, 2);

    // Act
    Page<FileInfoDTO> page = fileService.listFiles(pageable);

    // Assert
    assertNotNull(page);
    assertEquals(3, page.getTotalElements());
    assertEquals(2, page.getContent().size());
    assertEquals(2, page.getTotalPages());
    assertEquals(0, page.getNumber());
    assertEquals(2, page.getSize());

    List<FileInfoDTO> files = page.getContent();
    assertEquals("file1.jpg", files.get(0).getFileName());
    assertEquals("file2.jpg", files.get(1).getFileName());

    pageable = PageRequest.of(1, 2);
    page = fileService.listFiles(pageable);
    assertEquals(1, page.getContent().size());
    assertEquals("file3.jpg", page.getContent().getFirst().getFileName());
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
}
