package br.com.portifolio.dto.response;

import br.com.portifolio.enums.MemberRole;
import br.com.portifolio.enums.ProjectStatus;
import br.com.portifolio.enums.RiskLevel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ProjectResponse {

    private Long id;
    private String nome;
    private LocalDate dataInicio;
    private LocalDate previsaoTermino;
    private LocalDate dataRealTermino;
    private BigDecimal orcamentoTotal;
    private String descricao;
    private Long gerenteId;
    private String gerenteNome;
    private ProjectStatus status;
    private RiskLevel nivelRisco;
}
