package br.com.portifolio.dto.request;

import br.com.portifolio.enums.MemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalMemberRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull(message = "Atribuição é obrigatória")
    private MemberRole atribuicao;
}
