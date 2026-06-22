package br.com.portifolio.controller;

import br.com.portifolio.dto.request.ExternalMemberRequest;
import br.com.portifolio.dto.response.ExternalMemberResponse;
import br.com.portifolio.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/external/members")
@Tag(name = "API Externa de Membros (Mock)", description = "Simula o serviço externo de cadastro e consulta de membros")
public class ExternalMemberController {

    private final Map<Long, ExternalMemberResponse> members = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostMapping
    @Operation(summary = "Criar membro no serviço externo")
    public ResponseEntity<ExternalMemberResponse> create(@Valid @RequestBody ExternalMemberRequest request) {
        Long id = idGenerator.getAndIncrement();
        ExternalMemberResponse member = ExternalMemberResponse.builder()
                .id(id)
                .nome(request.getNome())
                .atribuicao(request.getAtribuicao())
                .build();
        members.put(id, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    @GetMapping
    @Operation(summary = "Listar membros do serviço externo")
    public List<ExternalMemberResponse> findAll() {
        return List.copyOf(members.values());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar membro por ID no serviço externo")
    public ExternalMemberResponse findById(@PathVariable Long id) {
        return Optional.ofNullable(members.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Membro externo não encontrado: " + id));
    }
}
