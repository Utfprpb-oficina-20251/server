package br.edu.utfpr.pb.ext.server.file;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Tag(name = "File", description = "API de gerenciamento de arquivos")
public class FileController {
  private final FileService fileService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Envia um arquivo para o servidor",
      description =
          "Envia um arquivo para o servidor, tipos suportados: image/jpeg, image/png, application/pdf")
  public ResponseEntity<FileInfoDTO> upload(@RequestPart("file") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok().body(fileService.store(file));
  }

  @GetMapping("/{filename:.+}")
  @Operation(
      summary = "Retorna um arquivo do servidor",
      description = "Retorna um arquivo do servidor. Use ?download=true para forçar o download.")
  public ResponseEntity<Resource> getFile(
      @Parameter(
              name = "filename",
              description = "Nome do arquivo no formato UUID.extensão",
              example = "2c6fc3e2-2bb7-4122-bf76-279ab507608f.jpg")
          @PathVariable
          String filename,
      @RequestParam(name = "download", defaultValue = "false") boolean download,
      HttpServletRequest request) {

    Resource file = fileService.loadFileAsResource(filename);
    String contentType = request.getServletContext().getMimeType(file.getFilename());

    if (contentType == null) {
      contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
    ResponseEntity.BodyBuilder response =
        ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType));
    if (download) {
      response.header("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"");
    }
    return response.body(file);
  }
}
