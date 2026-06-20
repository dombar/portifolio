package br.com.portifolio.controller;

import br.com.portifolio.dto.request.ProjectMemberAllocateRequest;
import br.com.portifolio.dto.response.MemberResponse;
import br.com.portifolio.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
@Tag(name = "Alocação de Membros", description = "Gerenciamento de membros alocados em projetos")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @GetMapping
    @Operation(summary = "Listar membros alocados no projeto")
    public List<MemberResponse> listMembers(@PathVariable Long projectId) {
        return projectMemberService.listMembers(projectId);
    }

    @PostMapping
    @Operation(summary = "Alocar membro ao projeto")
    public ResponseEntity<MemberResponse> allocateMember(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectMemberAllocateRequest request) {
        MemberResponse response = projectMemberService.allocateMember(projectId, request.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{memberId}")
    @Operation(summary = "Remover membro do projeto")
    public ResponseEntity<Void> removeMember(@PathVariable Long projectId, @PathVariable Long memberId) {
        projectMemberService.removeMember(projectId, memberId);
        return ResponseEntity.noContent().build();
    }
}
