package br.edu.utfpr.pb.ext.server.departamento.impl;

import br.edu.utfpr.pb.ext.server.departamento.Departamento;
import br.edu.utfpr.pb.ext.server.departamento.DepartamentoDTO;
import br.edu.utfpr.pb.ext.server.departamento.DepartamentoRepository;
import br.edu.utfpr.pb.ext.server.departamento.DepartamentoService;
import br.edu.utfpr.pb.ext.server.departamento.enums.Departamentos;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Implementação do serviço responsável pela associação de responsáveis aos departamentos. */
@Service
@RequiredArgsConstructor
public class DepartamentoServiceImpl implements DepartamentoService {

  // Repositório para persistência da entidade Departamento
  private final DepartamentoRepository repository;

  // Repositório para consulta de usuários
  private final UsuarioRepository usuarioRepository;

  /**
   * Salva a associação entre um departamento e um responsável.
   *
   * @param dto DTO contendo os dados da associação
   * @return DTO com os dados salvos
   */
  @Override
  public DepartamentoDTO save(DepartamentoDTO dto) {
    Departamento entity = new Departamento();
    entity.setId(dto.getId());
    entity.setDepartamento(dto.getDepartamento());

    // Busca o usuário responsável pelo ID fornecido no DTO
    Usuario responsavel = usuarioRepository.findById(dto.getResponsavelId()).orElse(null);
    entity.setResponsavel(responsavel);

    // Salva a entidade no banco de dados
    Departamento salvo = repository.save(entity);

    // Constrói o DTO de retorno com os dados da entidade salva
    DepartamentoDTO retorno = new DepartamentoDTO();
    retorno.setId(salvo.getId());
    retorno.setDepartamento(salvo.getDepartamento());
    retorno.setResponsavelId(salvo.getResponsavel().getId());
    return retorno;
  }

  /**
   * Retorna a lista de todas as associações entre departamentos e responsáveis.
   *
   * @return Lista de DTOs contendo as associações
   */
  @Override
  public List<DepartamentoDTO> findAll() {
    return repository.findAll().stream()
        .map(
            entity -> {
              DepartamentoDTO dto = new DepartamentoDTO();
              dto.setId(entity.getId());
              dto.setDepartamento(entity.getDepartamento());
              dto.setResponsavelId(entity.getResponsavel().getId());
              return dto;
            })
        .collect(Collectors.toList());
  }

  /**
   * Busca a associação de um departamento pelo nome do enum {@link Departamentos}.
   *
   * @param nome Nome do departamento (string que será convertida em enum)
   * @return DTO da associação encontrada ou null se não houver
   */
  @Override
  public DepartamentoDTO findByDepartamento(String nome) {
    Departamentos departamento = Departamentos.valueOf(nome);
    return repository
        .findByDepartamento(departamento)
        .map(
            entity -> {
              DepartamentoDTO dto = new DepartamentoDTO();
              dto.setId(entity.getId());
              dto.setDepartamento(entity.getDepartamento());
              dto.setResponsavelId(entity.getResponsavel().getId());
              return dto;
            })
        .orElse(null);
  }
}
