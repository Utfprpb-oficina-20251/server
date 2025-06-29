package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(
    name = "FiltroProjetoDTO",
    description = "DTO para encapsular os parâmetros de filtro de projetos.")
public record FiltroProjetoDTO(
    @Schema(
            description = "Filtrar por parte do título do projeto. A busca é case-insensitive.",
            example = "Robótica")
        String titulo,
    @Schema(description = "Filtrar projetos por um status específico.", example = "ATIVO")
        StatusProjeto status,
    @Schema(
            description = "Filtrar por projetos que iniciaram A PARTIR desta data.",
            example = "2024-01-01")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // Garante o parsing correto da data
        LocalDate dataInicioDe,
    @Schema(
            description = "Filtrar por projetos que iniciaram ATÉ esta data.",
            example = "2024-12-31")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataInicioAte,
    @Schema(
            description =
                "Filtrar por projetos que possuem um professor coordenador específico (usar o ID do usuário).",
            example = "10")
        Long idResponsavel,
    @Schema(
            description =
                "Filtrar por projetos que possuem um membro de equipe específico (usar o ID do usuário).",
            example = "25")
        Long idMembroEquipe,
    @Schema(
            description =
                "Filtrar por projetos vinculados a um curso específico (usar o ID do curso).",
            example = "5")
        Long idCurso,
    @Schema(
            description = "Filtrar projetos com carga horária MAIOR ou IGUAL a este valor.",
            example = "20")
        Long cargaHorariaMinima,
    @Schema(
            description = "Filtrar projetos com carga horária MENOR ou IGUAL a este valor.",
            example = "60")
        Long cargaHorariaMaxima,
    @Schema(
            description =
                "Filtrar por parte do nome do curso do responsável. A busca é case-insensitive.",
            example = "Software")
        String nomeCurso,
    @Schema(
            description =
                "Filtrar por projetos que possuem vagas. `true` para com vagas, `false` para sem vagas, não enviar para ignorar.",
            example = "true")
        Boolean temVagas) {}
