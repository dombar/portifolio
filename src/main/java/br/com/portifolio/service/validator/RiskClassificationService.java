package br.com.portifolio.service.validator;

import br.com.portifolio.enums.RiskLevel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class RiskClassificationService {

    private static final BigDecimal LIMITE_BAIXO = new BigDecimal("100000");
    private static final BigDecimal LIMITE_MEDIO = new BigDecimal("500000");

    @FunctionalInterface
    private interface RiskRule {
        boolean matches(BigDecimal budget, long months);
    }

    private static final List<Map.Entry<RiskRule, RiskLevel>> RULES = List.of(
            Map.entry(
                    (b, m) -> b.compareTo(LIMITE_MEDIO) > 0 || m > 6,
                    RiskLevel.ALTO),
            Map.entry(
                    (b, m) -> (b.compareTo(LIMITE_BAIXO) > 0 && b.compareTo(LIMITE_MEDIO) <= 0) || (m > 3 && m <= 6),
                    RiskLevel.MEDIO)
    );

    public RiskLevel classify(BigDecimal orcamento, LocalDate dataInicio, LocalDate previsaoTermino) {
        long months = ChronoUnit.MONTHS.between(dataInicio, previsaoTermino);
        return RULES.stream()
                .filter(e -> e.getKey().matches(orcamento, months))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(RiskLevel.BAIXO);
    }
}
