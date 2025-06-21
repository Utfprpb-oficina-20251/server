//package br.edu.utfpr.pb.ext.server.file;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.core.io.Resource;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/api/file")
//@RequiredArgsConstructor
//@Tag(name = "File", description = "API de gerenciamento de arquivos")
//public class FileController {
//  private final FileService fileService;
//
//  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//  @PreAuthorize("isAuthenticated()")
//  @Operation(
//      summary = "Envia um arquivo para o servidor",
//      description =
//          "Envia um arquivo para o servidor, tipos suportados: image/jpeg, image/png, application/pdf")
//  public FileInfoDTO upload(@RequestBody MultipartFile file) {
//    return fileService.store(file);
//  }
//
//  @GetMapping("/{filename:.+}")
//  public ResponseEntity<Resource> getFile(
//      @PathVariable String filename, HttpServletRequest request) {
//    Resource file = fileService.loadFileAsResource(filename);
//    String contentType = request.getServletContext().getMimeType(file.getFilename());
//    if (contentType == null) {
//      contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
//    }
//
//    return ResponseEntity.ok()
//        .contentType(MediaType.parseMediaType(contentType))
//        .header("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"")
//        .body(file);
//  }
//}
