package br.edu.utfpr.pb.ext.server.file;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoDTO {
  private String fileName;
  private String originalFileName;
  private String contentType;
  private long size;
  private String url;
  private LocalDateTime uploadDate;
}
