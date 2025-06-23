package br.edu.utfpr.pb.ext.server.file.img;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ImageUtils {
  public static final Pattern DATA_URI_PATTERN =
      Pattern.compile("^data:(image/(png|jpeg|jpg));base64,(.+)$", Pattern.CASE_INSENSITIVE);

  private static final Set<String> SUPPORTED_MIME_TYPES =
      Arrays.stream(ImageType.values()).map(ImageType::getMimeType).collect(Collectors.toSet());

  private final Tika tika = new Tika();

  @Value("${app.image.max-file-size:5242880}") // 5MB
  private long maxImageSize;

  public record DecodedImage(byte[] data, String contentType) {
    /**
     * Retorna uma representação em texto do objeto DecodedImage, incluindo o tamanho dos dados em
     * bytes e o tipo de conteúdo.
     *
     * @return uma string descritiva do DecodedImage
     */
    @Override
    public String toString() {
      return "DecodedImage{"
          + "dataSize="
          + (data != null ? data.length : 0)
          + " bytes"
          + ", contentType='"
          + contentType
          + '\''
          + '}';
    }

    /**
     * Compara este objeto DecodedImage com outro para verificar igualdade baseada nos dados da
     * imagem e no tipo de conteúdo.
     *
     * @param o o objeto a ser comparado com este DecodedImage
     * @return true se ambos os objetos possuem os mesmos dados e tipo de conteúdo; caso contrário,
     *     false
     */
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;

      if (o == null || getClass() != o.getClass()) return false;

      DecodedImage that = (DecodedImage) o;

      return new EqualsBuilder()
          .append(data(), that.data())
          .append(contentType(), that.contentType())
          .isEquals();
    }

    /**
     * Gera um código hash para o objeto DecodedImage com base nos dados da imagem e no tipo de
     * conteúdo.
     *
     * @return o código hash calculado.
     */
    @Override
    public int hashCode() {
      return new HashCodeBuilder(17, 37).append(data()).append(contentType()).toHashCode();
    }
  }

  /**
   * Valida e decodifica uma string de imagem em Base64, retornando um objeto {@code DecodedImage}
   * se a imagem for suportada e válida.
   *
   * <p>A função rejeita entradas nulas, vazias ou URLs HTTP/HTTPS. Aceita tanto strings em formato
   * data URI quanto Base64 puro. Decodifica a imagem, verifica o tamanho máximo permitido, detecta
   * o tipo MIME e valida se é suportado. Retorna {@code null} caso a validação falhe em qualquer
   * etapa.
   *
   * @param base64 string contendo a imagem em Base64 ou data URI.
   * @return um {@code DecodedImage} com os bytes e o tipo MIME da imagem, ou {@code null} se
   *     inválida ou não suportada.
   */
  public DecodedImage validateAndDecodeBase64Image(String base64) {
    if (base64 == null || base64.isBlank()) {
      return null;
    }

    try {
      URI uri = URI.create(base64);
      if (uri.getScheme() != null
          && (uri.getScheme().equalsIgnoreCase("http")
              || uri.getScheme().equalsIgnoreCase("https"))) {
        return null;
      }
    } catch (IllegalArgumentException e) {
      // Não é uma URL válida, tratamos como possível Base64
    }

    Matcher matcher = DATA_URI_PATTERN.matcher(base64);
    String base64Data;
    if (matcher.matches()) {
      base64Data = matcher.group(3);
    } else {
      base64Data = base64;
    }

    try {
      byte[] imageBytes = Base64.getDecoder().decode(base64Data);

      if (imageBytes.length > maxImageSize) {
        return null;
      }

      String mimeType = tika.detect(imageBytes);

      if (!isImageSupported(mimeType)) {
        return null;
      }

      BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
      if (img == null) {
        return null;
      }

      return new DecodedImage(imageBytes, mimeType);
    } catch (IllegalArgumentException | IOException e) {
      log.debug("Falha ao decodificar imagem Base64: {}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * Verifica se o tipo MIME fornecido é suportado para imagens.
   *
   * @param mimeType o tipo MIME a ser verificado
   * @return {@code true} se o tipo MIME for suportado; caso contrário, {@code false}
   */
  public static boolean isImageSupported(String mimeType) {
    return mimeType != null && SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase());
  }

  /**
   * Retorna a extensão de arquivo correspondente ao MIME type fornecido.
   *
   * <p>Se o MIME type for nulo ou não corresponder a nenhum tipo suportado, retorna a extensão
   * padrão para JPEG.
   *
   * @param mimeType o tipo MIME da imagem
   * @return a extensão de arquivo associada ao tipo MIME, ou "jpg" se não reconhecido
   */
  public static String getFileExtensionFromMimeType(String mimeType) {
    if (mimeType == null) {
      return ImageType.JPEG.getExtension(); // default
    }

    return Arrays.stream(ImageType.values())
        .filter(type -> type.getMimeType().equalsIgnoreCase(mimeType))
        .findFirst()
        .map(ImageType::getExtension)
        .orElse(ImageType.JPEG.getExtension()); // default to jpeg
  }
}
