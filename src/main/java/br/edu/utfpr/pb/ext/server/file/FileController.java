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

  /**
   * Realiza o upload de um arquivo para o servidor.
   *
   * Aceita arquivos nos formatos JPEG, PNG e PDF. Retorna informações sobre o arquivo armazenado em caso de sucesso. Retorna HTTP 400 se nenhum arquivo for enviado ou se o arquivo estiver vazio.
   *
   * @param file arquivo a ser enviado.
   * @return informações do arquivo armazenado.
   */
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

  /**
   * Recupera um arquivo armazenado no servidor pelo nome do arquivo.
   *
   * Retorna o arquivo solicitado como recurso, definindo o tipo de conteúdo conforme o arquivo. Se o parâmetro `download` for verdadeiro, o arquivo será enviado como anexo para download.
   *
   * @param filename Nome do arquivo, incluindo extensão, no formato UUID.extensão.
   * @param download Se verdadeiro, força o download do arquivo em vez de exibi-lo inline.
   * @return O arquivo solicitado como recurso HTTP.
   */
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
