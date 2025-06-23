package br.edu.utfpr.pb.ext.server.file.img;

import lombok.Getter;

@Getter
public enum ImageType {
  PNG("image/png", "png"),
  JPEG("image/jpeg", "jpg"),
  JPG("image/jpg", "jpg");

  private final String mimeType;
  private final String extension;

  /**
   * Constrói um tipo de imagem com o MIME type e a extensão de arquivo especificados.
   *
   * @param mimeType o tipo MIME associado ao formato de imagem
   * @param extension a extensão de arquivo correspondente ao formato de imagem
   */
  ImageType(String mimeType, String extension) {
    this.mimeType = mimeType;
    this.extension = extension;
  }

  /**
   * Verifica se o tipo MIME fornecido é suportado por algum dos formatos de imagem definidos.
   *
   * @param mimeType o tipo MIME a ser verificado
   * @return {@code true} se o tipo MIME corresponder a um dos formatos suportados; {@code false} caso contrário ou se {@code mimeType} for {@code null}
   */
  public static boolean isSupported(String mimeType) {
    if (mimeType == null) {
      return false;
    }

    for (ImageType type : values()) {
      if (type.getMimeType().equalsIgnoreCase(mimeType)) {
        return true;
      }
    }
    return false;
  }
}
