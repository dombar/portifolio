package br.com.portifolio.controller;

import br.com.portifolio.dto.request.ProjectCreateRequest;
import br.com.portifolio.dto.request.ProjectStatusUpdateRequest;
import br.com.portifolio.dto.request.ProjectUpdateRequest;
import br.com.portifolio.dto.response.ProjectResponse;
import br.com.portifolio.enums.ProjectStatus;
import br.com.portifolio.enums.RiskLevel;
import br.com.portifolio.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projetos", description = "Gerenciamento de projetos do portfólio")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Listar projetos com paginação e filtros")
    public Page<ProjectResponse> findAll(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long gerenteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicioDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicioAte,
            @RequestParam(required = false) RiskLevel nivelRisco,
            @PageableDefault(size = 20) Pageable pageable) {
        return projectService.findAll(status, nome, gerenteId, dataInicioDe, dataInicioAte, nivelRisco, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar projeto por ID")
    public ProjectResponse findById(@PathVariable Long id) {
        return projectService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Criar novo projeto")
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse response = projectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar projeto")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        return projectService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do projeto")
    public ProjectResponse updateStatus(@PathVariable Long id, @Valid @RequestBody ProjectStatusUpdateRequest request) {
        return projectService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir projeto")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
