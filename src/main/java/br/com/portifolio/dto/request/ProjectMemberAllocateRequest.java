package br.com.portifolio.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectMemberAllocateRequest {

    @NotNull(message = "ID do membro é obrigatório")
    private Long memberId;
}
