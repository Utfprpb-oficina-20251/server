package br.edu.utfpr.pb.ext.server.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

  @Mock private FileService fileService;

  @InjectMocks private FileController fileController;

  private Resource createNamedResource(String filename) {
    return new ByteArrayResource("test content".getBytes()) {
      @Override
      public String getFilename() {
        return filename;
      }
    };
  }

  @Test
  void upload_WhenFileIsEmpty_ShouldReturnBadRequest() {
    ResponseEntity<FileInfoDTO> response =
        fileController.upload(new MockMultipartFile("file", new byte[0]));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verifyNoInteractions(fileService);
  }

  @Test
  void upload_WhenFileIsValid_ShouldReturnFileInfoDTO() {
    MultipartFile validFile =
        new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

    FileInfoDTO expectedDto = new FileInfoDTO();
    when(fileService.store(any())).thenReturn(expectedDto);

    ResponseEntity<FileInfoDTO> response = fileController.upload(validFile);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(expectedDto, response.getBody());
    verify(fileService).store(validFile);
  }

  @ParameterizedTest
  @MethodSource("fileResponseScenarios")
  void getFile_ShouldReturnCorrectResponse(
      String filename,
      MediaType expectedContentType,
      boolean download,
      String expectedDisposition) {
    Resource mockResource = createNamedResource(filename);
    when(fileService.loadFileAsResource(filename)).thenReturn(mockResource);

    ResponseEntity<Resource> response =
        fileController.getFile(filename, download, new MockHttpServletRequest());

    assertEquals(expectedContentType, response.getHeaders().getContentType());
    if (expectedDisposition != null) {
      assertEquals(expectedDisposition, response.getHeaders().getFirst("Content-Disposition"));
    } else {
      assertNull(response.getHeaders().get("Content-Disposition"));
    }
  }

  private static Stream<Arguments> fileResponseScenarios() {
    return Stream.of(
        Arguments.of("test.jpg", MediaType.IMAGE_JPEG, false, null, true),
        Arguments.of(
            "document.pdf",
            MediaType.APPLICATION_PDF,
            true,
            "attachment; filename=\"document.pdf\"",
            true),
        Arguments.of("file.xyz", MediaType.parseMediaType("chemical/x-xyz"), false, null, false));
  }
}
