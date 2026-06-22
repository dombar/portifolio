package br.com.portifolio.dto.response;

import br.com.portifolio.enums.MemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExternalMemberResponse {

    private Long id;
    private String nome;
    private MemberRole atribuicao;
}
