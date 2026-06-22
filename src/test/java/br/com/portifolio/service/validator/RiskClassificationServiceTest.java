package br.com.portifolio.service.validator;

import br.com.portifolio.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RiskClassificationServiceTest {

    private RiskClassificationService service;

    @BeforeEach
    void setUp() {
        service = new RiskClassificationService();
    }

    @Test
    @DisplayName("Should classify as LOW when budget and duration are within limits")
    void shouldClassifyAsLowRisk() {
        RiskLevel result = service.classify(
                new BigDecimal("50000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 1));

        assertThat(result).isEqualTo(RiskLevel.BAIXO);
    }

    @Test
    @DisplayName("Should classify as MEDIUM when budget is in medium range")
    void shouldClassifyAsMediumRiskByBudget() {
        RiskLevel result = service.classify(
                new BigDecimal("200000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1));

        assertThat(result).isEqualTo(RiskLevel.MEDIO);
    }

    @Test
    @DisplayName("Should classify as MEDIUM when duration is between 3 and 6 months")
    void shouldClassifyAsMediumRiskByDuration() {
        RiskLevel result = service.classify(
                new BigDecimal("50000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 1));

        assertThat(result).isEqualTo(RiskLevel.MEDIO);
    }

    @Test
    @DisplayName("Should classify as HIGH when budget exceeds limit")
    void shouldClassifyAsHighRiskByBudget() {
        RiskLevel result = service.classify(
                new BigDecimal("600000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1));

        assertThat(result).isEqualTo(RiskLevel.ALTO);
    }

    @Test
    @DisplayName("Should classify as HIGH when duration exceeds 6 months")
    void shouldClassifyAsHighRiskByDuration() {
        RiskLevel result = service.classify(
                new BigDecimal("50000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 8, 1));

        assertThat(result).isEqualTo(RiskLevel.ALTO);
    }

    @ParameterizedTest
    @CsvSource({
            "100000, 2026-01-01, 2026-04-01, BAIXO",
            "100001, 2026-01-01, 2026-02-01, MEDIO",
            "500001, 2026-01-01, 2026-02-01, ALTO"
    })
    @DisplayName("Should correctly classify boundary values")
    void shouldClassifyBoundaryValues(BigDecimal budget, String start, String end, RiskLevel expected) {
        RiskLevel result = service.classify(
                budget,
                LocalDate.parse(start),
                LocalDate.parse(end));

        assertThat(result).isEqualTo(expected);
    }
}
