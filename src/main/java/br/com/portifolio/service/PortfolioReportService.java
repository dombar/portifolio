package br.com.portifolio.service;

import br.com.portifolio.dto.response.PortfolioReportResponse;
import br.com.portifolio.enums.ProjectStatus;
import br.com.portifolio.repository.ProjectMemberRepository;
import br.com.portifolio.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioReportService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional(readOnly = true)
    public PortfolioReportResponse generateReport() {
        Map<ProjectStatus, Long> quantidadePorStatus = initStatusMap(0L);
        projectRepository.countByStatus().forEach(row -> {
            quantidadePorStatus.put((ProjectStatus) row[0], (Long) row[1]);
        });

        Map<ProjectStatus, BigDecimal> totalOrcadoPorStatus = initStatusMap(BigDecimal.ZERO);
        projectRepository.sumBudgetByStatus().forEach(row -> {
            totalOrcadoPorStatus.put((ProjectStatus) row[0], (BigDecimal) row[1]);
        });

        return PortfolioReportResponse.builder()
                .quantidadePorStatus(quantidadePorStatus)
                .totalOrcadoPorStatus(totalOrcadoPorStatus)
                .mediaDuracaoEncerrados(projectRepository.averageDurationOfClosedProjects())
                .totalMembrosUnicosAlocados(projectMemberRepository.countDistinctMembers())
                .build();
    }

    private <T> Map<ProjectStatus, T> initStatusMap(T defaultValue) {
        return Arrays.stream(ProjectStatus.values())
                .collect(Collectors.toMap(
                        Function.identity(),
                        s -> defaultValue,
                        (a, b) -> a,
                        () -> new EnumMap<>(ProjectStatus.class)));
    }
}
