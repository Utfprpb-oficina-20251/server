package br.edu.utfpr.pb.ext.server.sugestaoprojeto.service;

import br.edu.utfpr.pb.ext.server.file.FileInfoDTO;
import br.edu.utfpr.pb.ext.server.file.FileService;
import br.edu.utfpr.pb.ext.server.file.img.ImageUtils;
import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.*;
import br.edu.utfpr.pb.ext.server.usuario.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SugestaoDeProjetoServiceImpl extends CrudServiceImpl<SugestaoDeProjeto, Long>
    implements ISugestaoDeProjetoService {

  private final SugestaoDeProjetoRepository repository;
  private final UsuarioRepository usuarioRepository;
  private final IUsuarioService usuarioService;
  private final FileService fileService;
  private final ImageUtils imageUtils;

  /**
   * Fornece o repositório específico para operações CRUD da entidade SugestaoDeProjeto.
   *
   * @return o repositório de SugestaoDeProjeto
   */
  @Override
  protected JpaRepository<SugestaoDeProjeto, Long> getRepository() {
    return repository;
  }

  /**
   * Prepara a entidade SugestaoDeProjeto para persistência, associando o usuário logado como aluno,
   * definindo o status como AGUARDANDO, validando o professor informado e processando a imagem, se
   * presente.
   *
   * <p>Caso um professor seja especificado, valida sua existência e papel; lança
   * EntityNotFoundException se não encontrado. Se houver imagem em formato Base64, realiza o
   * processamento e armazenamento, atualizando o campo correspondente.
   *
   * @param entity sugestão de projeto a ser preparada para salvamento
   * @return a entidade SugestaoDeProjeto pronta para persistência
   */
  @Override
  public SugestaoDeProjeto preSave(SugestaoDeProjeto entity) {

    Usuario aluno = usuarioService.obterUsuarioLogado();

    entity.setStatus(StatusSugestao.AGUARDANDO);
    entity.setAluno(aluno);

    if (entity.getProfessor() != null && entity.getProfessor().getId() != null) {
      Usuario professor =
          usuarioRepository
              .findById(entity.getProfessor().getId())
              .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado"));

      usuarioService.validarProfessor(professor);
      entity.setProfessor(professor);
    }
    processarImagemUrl(entity);

    return super.preSave(entity);
  }

  /**
   * Processa e armazena a imagem associada à sugestão de projeto, caso o campo imagemUrl contenha
   * uma string em Base64.
   *
   * <p>Se a imagem for válida e puder ser decodificada, ela é salva e o campo imagemUrl é
   * atualizado com a URL do arquivo armazenado. Lança uma ResponseStatusException com status 500 em
   * caso de falha no processamento ou armazenamento da imagem.
   *
   * @param sugestao Entidade SugestaoDeProjeto que pode conter uma imagem em Base64 no campo
   *     imagemUrl.
   */
  private void processarImagemUrl(SugestaoDeProjeto sugestao) {
    String imagemUrl = sugestao.getImagemUrl();
    if (imagemUrl == null || imagemUrl.isBlank()) {
      return;
    }

    ImageUtils.DecodedImage decodedImage = imageUtils.validateAndDecodeBase64Image(imagemUrl);
    if (decodedImage != null) {
      try {
        String filename =
            "sugestao-imagem."
                + ImageUtils.getFileExtensionFromMimeType(decodedImage.contentType());
        FileInfoDTO fileInfo =
            fileService.store(decodedImage.data(), decodedImage.contentType(), filename);
        sugestao.setImagemUrl(fileInfo.getUrl());
      } catch (Exception e) {
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Falha ao processar a imagem da sugestão de projeto.",
            e);
      }
    }
  }

  /**
   * Lista as sugestões de projeto vinculadas ao aluno identificado pelo ID fornecido.
   *
   * <p>O acesso é restrito a usuários com o papel "ROLE_SERVIDOR" ou ao próprio aluno.
   *
   * @param alunoId identificador do aluno cujas sugestões de projeto serão retornadas
   * @return lista de sugestões de projeto associadas ao aluno
   */
  @PreAuthorize("hasRole('ROLE_SERVIDOR') or #alunoId == authentication.principal.id")
  public List<SugestaoDeProjeto> listarPorAluno(Long alunoId) {
    return repository.findByAlunoId(alunoId);
  }

  /**
   * Recupera todas as sugestões de projeto vinculadas ao usuário atualmente autenticado.
   *
   * @return lista de sugestões de projeto associadas ao usuário logado
   */
  public List<SugestaoDeProjeto> listarSugestoesDoUsuarioLogado() {
    Usuario usuario = usuarioService.obterUsuarioLogado();
    return repository.findByAlunoId(usuario.getId());
  }

  /**
   * Recupera todas as sugestões de projeto indicadas para o usuário atualmente autenticado.
   *
   * @return lista de sugestões de projeto indicadas para o usuário logado
   */
  public List<SugestaoDeProjeto> listarIndicacoesDoUsuarioLogado() {
    Usuario usuario = usuarioService.obterUsuarioLogado();
    return repository.findByProfessorId(usuario.getId());
  }
}
