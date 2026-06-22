package br.com.portifolio.dto.response;

import br.com.portifolio.enums.MemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {

    private Long id;
    private Long externalId;
    private String nome;
    private MemberRole atribuicao;
}
