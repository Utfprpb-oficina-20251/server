package br.edu.utfpr.pb.ext.server.file.img;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    @Override
    public String toString() {
      return "DecodedImage{"
          + "data="
          + Arrays.toString(data)
          + ", contentType='"
          + contentType
          + '\''
          + '}';
    }

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

    @Override
    public int hashCode() {
      return new HashCodeBuilder(17, 37).append(data()).append(contentType()).toHashCode();
    }
  }

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
      // URL é inválida, continua
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
    } catch (Exception e) {
      return null;
    }
  }

  public static boolean isImageSupported(String mimeType) {
    return mimeType != null && SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase());
  }

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
