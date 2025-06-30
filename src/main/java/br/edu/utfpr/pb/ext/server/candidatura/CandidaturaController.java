package br.edu.utfpr.pb.ext.server.candidatura;

import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidaturas")
@RequiredArgsConstructor
@Tag(name = "Candidaturas", description = "API para gerenciamento de candidaturas a projetos")
public class CandidaturaController {

    private final ICandidaturaService candidaturaService;
    private final ModelMapper modelMapper;

    @PostMapping("/{projetoId}")
    @Operation(
            summary = "Candidatar-se a um projeto existente",
            description = "Cria uma nova candidatura para o projeto informado.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Candidatura realizada com sucesso")
            })
    public ResponseEntity<CandidaturaDTO> candidatar(@PathVariable Long projetoId) {
        Candidatura candidatura = candidaturaService.candidatar(projetoId);
        return ResponseEntity.ok(modelMapper.map(candidatura, CandidaturaDTO.class));
    }

    @GetMapping("/minhas-candidaturas")
    @Operation(
            summary = "Listar minhas candidaturas",
            description = "Retorna todas as candidaturas do usuário logado.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de candidaturas retornada com sucesso")
            })
    public ResponseEntity<List<CandidaturaDTO>> listarMinhasCandidaturas(
            @AuthenticationPrincipal Usuario usuario) {
        List<Candidatura> candidaturas = candidaturaService.findAllByAlunoId(usuario.getId());
        List<CandidaturaDTO> candidaturaDTOs =
                candidaturas.stream()
                        .map(candidatura -> modelMapper.map(candidatura, CandidaturaDTO.class))
                        .toList();
        return ResponseEntity.ok(candidaturaDTOs);
    }

    @PreAuthorize("hasRole('SERVIDOR')")
    @GetMapping("/{projetoId}")
    @Operation(
            summary = "Listar candidaturas de um projeto",
            description = "Retorna todas as candidaturas para o projeto informado.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de candidaturas retornada com sucesso"),
                    @ApiResponse(responseCode = "200", description = "Lista vazia quando não há candidaturas para o projeto")
            })
    public ResponseEntity<List<CandidaturaDTO>> listarCandidaturasPorProjeto(
            @PathVariable Long projetoId) {
        List<Candidatura> candidaturas = candidaturaService.findAllPendentesByProjetoId(projetoId);
        if (candidaturas.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<CandidaturaDTO> candidaturaDTOs =
                candidaturas.stream()
                        .map(candidatura -> modelMapper.map(candidatura, CandidaturaDTO.class))
                        .toList();
        return ResponseEntity.ok(candidaturaDTOs);
    }

    @PreAuthorize("hasRole('SERVIDOR')")
    @PutMapping("/{projetoId}/atualizar-status")
    @Operation(
            summary = "Atualizar status das candidaturas",
            description = "Atualiza o status de uma lista de candidaturas.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Status das candidaturas atualizado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Lista de candidaturas vazia ou inválida"),
                    @ApiResponse(responseCode = "404", description = "Candidatura não encontrada")
            })
    public ResponseEntity<String> atualizarStatusCandidaturas(
            @PathVariable Long projetoId, @RequestBody List<CandidaturaDTO> candidaturaDTOs) {
        for (CandidaturaDTO candidaturaDTO : candidaturaDTOs) {
            if (!candidaturaDTO.getProjetoId().equals(projetoId)) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body("O ID do projeto na candidatura não corresponde ao projeto informado na URL");
            }
        }
        List<Candidatura> candidaturas =
                candidaturaDTOs.stream().map(dto -> modelMapper.map(dto, Candidatura.class)).toList();
        candidaturaService.atualizarStatusCandidaturas(candidaturas);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{candidaturaId}/cancelar")
    @Operation(summary = "Cancelar candidatura", description = "Cancela uma candidatura existente.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Candidatura cancelada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Candidatura não encontrada")
            })
    public ResponseEntity<String> cancelarCandidatura(
            @PathVariable Long candidaturaId, @AuthenticationPrincipal Usuario usuario) {
        Candidatura candidatura = candidaturaService.findById(candidaturaId);
        if (candidatura == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Candidatura com ID " + candidaturaId + " não encontrada");
        }
        if (!candidatura.getAluno().getId().equals(usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você não tem permissão para cancelar esta candidatura");
        }

        candidatura.setStatus(StatusCandidatura.CANCELADA);
        candidaturaService.atualizarStatusCandidaturas(List.of(candidatura));

        return ResponseEntity.ok("Candidatura cancelada com sucesso");
    }
}
