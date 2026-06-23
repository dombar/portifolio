package br.com.portifolio.controller;

import br.com.portifolio.dto.response.PortfolioReportResponse;
import br.com.portifolio.service.PortfolioReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Relatórios do portfólio")
public class PortfolioReportController {

    private final PortfolioReportService portfolioReportService;

    @GetMapping("/portfolio")
    @Operation(summary = "Gerar relatório resumido do portfólio")
    public PortfolioReportResponse getPortfolioReport() {
        return portfolioReportService.generateReport();
    }
}
