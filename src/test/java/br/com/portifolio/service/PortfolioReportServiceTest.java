package br.com.portifolio.service;

import br.com.portifolio.dto.response.PortfolioReportResponse;
import br.com.portifolio.enums.ProjectStatus;
import br.com.portifolio.repository.ProjectMemberRepository;
import br.com.portifolio.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioReportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private PortfolioReportService portfolioReportService;

    @Test
    @DisplayName("Should generate report with aggregated data")
    void shouldGenerateReport() {
        when(projectRepository.countByStatus()).thenReturn(List.of(
                new Object[]{ProjectStatus.EM_ANALISE, 2L},
                new Object[]{ProjectStatus.ENCERRADO, 1L}
        ));
        when(projectRepository.sumBudgetByStatus()).thenReturn(List.of(
                new Object[]{ProjectStatus.EM_ANALISE, new BigDecimal("150000")},
                new Object[]{ProjectStatus.ENCERRADO, new BigDecimal("80000")}
        ));
        when(projectRepository.averageDurationOfClosedProjects()).thenReturn(90.0);
        when(projectMemberRepository.countDistinctMembers()).thenReturn(5L);

        PortfolioReportResponse report = portfolioReportService.generateReport();

        assertThat(report.getQuantidadePorStatus().get(ProjectStatus.EM_ANALISE)).isEqualTo(2L);
        assertThat(report.getQuantidadePorStatus().get(ProjectStatus.ENCERRADO)).isEqualTo(1L);
        assertThat(report.getQuantidadePorStatus().get(ProjectStatus.INICIADO)).isEqualTo(0L);
        assertThat(report.getTotalOrcadoPorStatus().get(ProjectStatus.EM_ANALISE))
                .isEqualByComparingTo(new BigDecimal("150000"));
        assertThat(report.getTotalOrcadoPorStatus().get(ProjectStatus.INICIADO))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(report.getMediaDuracaoEncerrados()).isEqualTo(90.0);
        assertThat(report.getTotalMembrosUnicosAlocados()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should generate empty report with all statuses zeroed when no projects exist")
    void shouldGenerateEmptyReport() {
        when(projectRepository.countByStatus()).thenReturn(List.of());
        when(projectRepository.sumBudgetByStatus()).thenReturn(List.of());
        when(projectRepository.averageDurationOfClosedProjects()).thenReturn(null);
        when(projectMemberRepository.countDistinctMembers()).thenReturn(0L);

        PortfolioReportResponse report = portfolioReportService.generateReport();

        assertThat(report.getQuantidadePorStatus()).hasSize(ProjectStatus.values().length);
        assertThat(report.getQuantidadePorStatus().values())
                .allSatisfy(v -> assertThat(v).isEqualTo(0L));
        assertThat(report.getTotalOrcadoPorStatus()).hasSize(ProjectStatus.values().length);
        assertThat(report.getTotalOrcadoPorStatus().values())
                .allSatisfy(v -> assertThat(v).isEqualByComparingTo(BigDecimal.ZERO));
        assertThat(report.getMediaDuracaoEncerrados()).isNull();
        assertThat(report.getTotalMembrosUnicosAlocados()).isEqualTo(0L);
    }
}
