package br.com.portifolio.controller;

import br.com.portifolio.dto.response.MemberResponse;
import br.com.portifolio.service.MemberSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Membros", description = "Sincronização de membros da API externa")
public class MemberSyncController {

    private final MemberSyncService memberSyncService;

    @PostMapping("/sync/{externalId}")
    @Operation(summary = "Sincronizar membro da API externa")
    public ResponseEntity<MemberResponse> syncMember(@PathVariable Long externalId) {
        return ResponseEntity.ok(memberSyncService.syncMember(externalId));
    }
}
