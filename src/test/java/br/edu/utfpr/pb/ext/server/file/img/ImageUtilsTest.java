package br.edu.utfpr.pb.ext.server.file.img;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.image.max-file-size=5242880"})
class ImageUtilsTest {

  @Autowired private ImageUtils imageUtils;

  @Test
  @DisplayName("Deve retornar nulo quando a string base64 for nula ou em branco")
  void validateAndDecodeBase64Image_NullOrBlank_ReturnsNull() {
    assertNull(imageUtils.validateAndDecodeBase64Image(null));
    assertNull(imageUtils.validateAndDecodeBase64Image(""));
    assertNull(imageUtils.validateAndDecodeBase64Image("   "));
  }

  @Test
  @DisplayName("Deve retornar nulo para URIs HTTP/HTTPS válidas")
  void validateAndDecodeBase64Image_ValidHttpUri_ReturnsNull() {
    assertNull(imageUtils.validateAndDecodeBase64Image("http://example.com/image.png"));
    assertNull(imageUtils.validateAndDecodeBase64Image("https://example.com/image.png"));
  }

  @Test
  @DisplayName("Deve retornar nulo para string Base64 inválida")
  void validateAndDecodeBase64Image_InvalidBase64String_ReturnsNull() {
    assertNull(imageUtils.validateAndDecodeBase64Image("invalid_base64"));
    assertNull(
        imageUtils.validateAndDecodeBase64Image("data:image/png;base64,invalid_base64_string!@#"));
  }

  @Test
  @DisplayName("Deve retornar nulo quando o tamanho da imagem exceder o máximo permitido")
  void validateAndDecodeBase64Image_ImageExceedsMaxSize_ReturnsNull() {
    String largeBase64Image = Base64.getEncoder().encodeToString(new byte[10_000_000]);
    assertNull(imageUtils.validateAndDecodeBase64Image(largeBase64Image));
  }

  @Test
  @DisplayName("Deve retornar nulo para dados Base64 vazios")
  void validateAndDecodeBase64Image_EmptyBase64Data_ReturnsNull() {
    String base64Image = Base64.getEncoder().encodeToString(new byte[0]);
    assertNull(imageUtils.validateAndDecodeBase64Image(base64Image));
  }

  @Test
  @DisplayName("Deve retornar nulo para dados de imagem inválidos (não reconhecíveis pelo ImageIO)")
  void validateAndDecodeBase64Image_InvalidImageData_ReturnsNull() {
    String base64Image = Base64.getEncoder().encodeToString("notImageData".getBytes());
    assertNull(imageUtils.validateAndDecodeBase64Image(base64Image));
  }

  @Test
  @DisplayName("Deve decodificar e validar uma imagem PNG Base64 válida com URI de dados")
  void validateAndDecodeBase64Image_ValidPngImage_ReturnsDecodedImage() {
    // 1x1 red pixel PNG in Base64
    String validPngBase64 =
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/wcAAwAB/epv2AAAAABJRU5ErkJggg==";
    String dataUri = "data:image/png;base64," + validPngBase64;

    ImageUtils.DecodedImage decodedImage = imageUtils.validateAndDecodeBase64Image(dataUri);

    assertNotNull(decodedImage);
    assertNotNull(decodedImage.data());
    assertEquals("image/png", decodedImage.contentType());
  }

  @Test
  @DisplayName("Deve decodificar e validar uma imagem PNG Base64 válida sem URI de dados")
  void validateAndDecodeBase64Image_ValidPngImageWithoutDataUri_ReturnsDecodedImage() {
    // 1x1 red pixel PNG in Base64
    String validPngBase64 =
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/wcAAwAB/epv2AAAAABJRU5ErkJggg==";

    ImageUtils.DecodedImage decodedImage = imageUtils.validateAndDecodeBase64Image(validPngBase64);

    assertNotNull(decodedImage);
    assertNotNull(decodedImage.data());
    assertEquals("image/png", decodedImage.contentType());
  }

  @Test
  @DisplayName("Deve retornar verdadeiro para tipos MIME de imagem suportados")
  void isImageSupported_ValidMimeTypes_ReturnsTrue() {
    assertTrue(ImageUtils.isImageSupported("image/png"));
    assertTrue(ImageUtils.isImageSupported("image/jpeg"));
    assertTrue(ImageUtils.isImageSupported("image/jpg"));
    assertTrue(ImageUtils.isImageSupported("IMAGE/PNG")); // case insensitive
  }

  @Test
  @DisplayName("Deve retornar falso para tipos MIME de imagem não suportados ou inválidos")
  void isImageSupported_InvalidMimeTypes_ReturnsFalse() {
    assertFalse(ImageUtils.isImageSupported("image/gif"));
    assertFalse(ImageUtils.isImageSupported("image/bmp"));
    assertFalse(ImageUtils.isImageSupported("text/plain"));
    assertFalse(ImageUtils.isImageSupported(null));
    assertFalse(ImageUtils.isImageSupported(""));
  }

  @Test
  @DisplayName("Deve retornar a extensão de arquivo correta para tipos MIME válidos")
  void getFileExtensionFromMimeType_ValidMimeTypes_ReturnsCorrectExtensions() {
    assertEquals("png", ImageUtils.getFileExtensionFromMimeType("image/png"));
    assertEquals("jpg", ImageUtils.getFileExtensionFromMimeType("image/jpeg"));
    assertEquals("jpg", ImageUtils.getFileExtensionFromMimeType("image/jpg"));
    assertEquals("png", ImageUtils.getFileExtensionFromMimeType("IMAGE/PNG")); // case insensitive
    assertEquals(
        "jpg",
        ImageUtils.getFileExtensionFromMimeType("image/gif")); // unsupported, defaults to jpg
    assertEquals("jpg", ImageUtils.getFileExtensionFromMimeType(null)); // null, defaults to jpg
  }

  @Test
  @DisplayName("O método isSupported do ImageType deve funcionar corretamente")
  void imageType_isSupported_WorksCorrectly() {
    assertTrue(ImageType.isSupported("image/png"));
    assertTrue(ImageType.isSupported("image/jpeg"));
    assertTrue(ImageType.isSupported("image/jpg"));
    assertTrue(ImageType.isSupported("IMAGE/PNG")); // case insensitive
    assertFalse(ImageType.isSupported("image/gif"));
    assertFalse(ImageType.isSupported(null));
  }

  @Test
  @DisplayName("Os valores do enum ImageType devem ter propriedades corretas")
  void imageType_enumValues_HaveCorrectProperties() {
    assertEquals("image/png", ImageType.PNG.getMimeType());
    assertEquals("png", ImageType.PNG.getExtension());

    assertEquals("image/jpeg", ImageType.JPEG.getMimeType());
    assertEquals("jpg", ImageType.JPEG.getExtension());

    assertEquals("image/jpg", ImageType.JPG.getMimeType());
    assertEquals("jpg", ImageType.JPG.getExtension());
  }
}
