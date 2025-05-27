package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.departamento.enums.Departamentos;
import java.util.List;

/**
 * Interface que define o contrato do serviço de associação de responsáveis aos departamentos.
 * Contém os métodos que devem ser implementados para manipular as associações.
 */
public interface DepartamentoService {

  /**
   * Salva uma nova associação ou atualiza uma existente entre departamento e responsável.
   *
   * @param dto Objeto com os dados da associação
   * @return DTO da associação salva
   */
  DepartamentoDTO save(DepartamentoDTO dto);

  /**
   * Retorna a lista de todas as associações entre departamentos e responsáveis.
   *
   * @return Lista de DTOs com as associações cadastradas
   */
  List<DepartamentoDTO> findAll();

  /**
   * Busca uma associação a partir do nome do departamento.
   *
   * @param nome Nome do departamento (deve ser um valor válido do enum {@link Departamentos})
   * @return DTO da associação encontrada, ou null se não existir
   */
  DepartamentoDTO findByDepartamento(String nome);
}
