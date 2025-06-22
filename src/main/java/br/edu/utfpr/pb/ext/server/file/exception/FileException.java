package br.edu.utfpr.pb.ext.server.file.exception;

public class FileException extends RuntimeException {
  /**
   * Cria uma nova exceção de arquivo com a mensagem especificada.
   *
   * @param message a mensagem detalhada da exceção.
   */
  public FileException(String message) {
    super(message);
  }

  /**
   * Cria uma nova instância de FileException com uma mensagem detalhada e uma causa.
   *
   * @param message mensagem descritiva do erro relacionado à operação de arquivo.
   * @param cause exceção que causou esta exceção, permitindo o encadeamento de erros.
   */
  public FileException(String message, Throwable cause) {
    super(message, cause);
  }
}
