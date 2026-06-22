package br.com.portifolio.service.validator;

import br.com.portifolio.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectAllocationValidatorTest {

    private ProjectAllocationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ProjectAllocationValidator();
    }

    @Test
    @DisplayName("Should allow allocation when below maximum")
    void shouldAllowAllocationBelowMax() {
        assertThatCode(() -> validator.validateMemberCountForAllocation(5))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject allocation when reaching maximum")
    void shouldRejectAllocationAtMax() {
        assertThatThrownBy(() -> validator.validateMemberCountForAllocation(10))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("máximo de 10");
    }

    @Test
    @DisplayName("Should allow removal when above minimum")
    void shouldAllowRemovalAboveMin() {
        assertThatCode(() -> validator.validateMemberCountForRemoval(2))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject removal when at minimum")
    void shouldRejectRemovalAtMin() {
        assertThatThrownBy(() -> validator.validateMemberCountForRemoval(1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no mínimo 1");
    }

    @Test
    @DisplayName("Should allow allocation in active projects below limit")
    void shouldAllowActiveProjectsBelowLimit() {
        assertThatCode(() -> validator.validateActiveProjectsLimit(2))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject allocation when member reaches 3 active projects")
    void shouldRejectActiveProjectsAtLimit() {
        assertThatThrownBy(() -> validator.validateActiveProjectsLimit(3))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("3 projetos ativos");
    }
}
