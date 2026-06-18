package br.com.portifolio.dto.response;

import br.com.portifolio.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
public class PortfolioReportResponse {

    private Map<ProjectStatus, Long> quantidadePorStatus;
    private Map<ProjectStatus, BigDecimal> totalOrcadoPorStatus;
    private Double mediaDuracaoEncerrados;
    private Long totalMembrosUnicosAlocados;
}
