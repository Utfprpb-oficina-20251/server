
package br.edu.utfpr.pb.ext.server.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import br.edu.utfpr.pb.ext.server.usuario.enums.Departamentos;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueCpf;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueSiape;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;

@Schema(
    name = "UsuarioServidorRequestDTO",
    description = "Dados para cadastro ou atualização de um servidor",
    title = "Cadastro de Servidor",
    example = "{\"nomeCompleto\":\"João da Silva\",\"cpf\":\"123.456.789-00\",\"siape\":\"1234567\",\"emailInstitucional\":\"joao@utfpr.edu.br\",\"departamento\":\"DACOM\"}"
)
@Data
public class UsuarioServidorRequestDTO {

    @Schema(
        description = "Identificador único do servidor (null para novos cadastros)",
        example = "1",
        nullable = true
    )
    private Long id;

    @Schema(
        description = "Nome completo do servidor",
        example = "João da Silva",
        required = true
    )
    @NotNull
    private String nomeCompleto;

    @Schema(
        description = "CPF do servidor (deve ser único e válido)",
        example = "123.456.789-00",
        required = true,
        pattern = "^\\d{3}\\.\\d{3}\\.\\d{3}\\-\\d{2}$"
    )
    @NotNull
    @CPF
    @UniqueCpf
    private String cpf;

    @Schema(
        description = "Número SIAPE do servidor (7 dígitos)",
        example = "1234567",
        required = true,
        minLength = 7,
        maxLength = 7,
        pattern = "^\\d{7}$"
    )
    @NotNull
    @UniqueSiape
    @Size(
        min = 7,
        max = 7,
        message = "{br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorRequestDTO.siape}"
    )
    private String siape;

    @Schema(
        description = "E-mail institucional do servidor (domínio utfpr.edu.br)",
        example = "joao@utfpr.edu.br",
        required = true,
        format = "email",
        pattern = "^[a-zA-Z0-9._%+-]+@(utfpr\\.edu\\.br)$"
    )
    @NotNull
    @Email(
        regexp = "^[a-zA-Z0-9._%+-]+@(utfpr\\.edu\\.br)$",
        message = "{br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorRequestDTO.emailInstitucional}"
    )
    private String emailInstitucional;

    @Schema(
        description = "Telefone do servidor (11 dígitos)",
        example = "41999999999",
        nullable = true,
        minLength = 11,
        maxLength = 11,
        pattern = "^\\d{11}$"
    )
    @Size(min = 11, max = 11)
    private String telefone;

    @Schema(
        description = "Endereço completo do servidor",
        example = "Rua Exemplo, 123, Bairro Centro, Pato Branco/PR",
        nullable = true,
        minLength = 3,
        maxLength = 100
    )
    @Size(min = 3, max = 100)
    private String enderecoCompleto;

    @Schema(
        description = "Departamento ao qual o servidor está vinculado",
        example = "DACOM",
        required = true,
        implementation = Departamentos.class
    )
    @NotNull
    private Departamentos departamento;
}
