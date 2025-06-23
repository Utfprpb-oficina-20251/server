package br.edu.utfpr.pb.ext.server.file.img;

import lombok.Getter;

@Getter
public enum ImageType {
  PNG("image/png", "png"),
  JPEG("image/jpeg", "jpg"),
  JPG("image/jpg", "jpg");

  private final String mimeType;
  private final String extension;

  ImageType(String mimeType, String extension) {
    this.mimeType = mimeType;
    this.extension = extension;
  }

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
