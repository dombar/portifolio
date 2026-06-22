package br.com.portifolio.service.validator;

import br.com.portifolio.enums.ProjectStatus;
import br.com.portifolio.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectStatusTransitionValidatorTest {

    private ProjectStatusTransitionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ProjectStatusTransitionValidator();
    }

    @Test
    @DisplayName("Should allow valid sequential transition")
    void shouldAllowValidSequentialTransition() {
        assertThatCode(() -> validator.validate(ProjectStatus.EM_ANALISE, ProjectStatus.ANALISE_REALIZADA))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow cancellation from any active status")
    void shouldAllowCancellationFromActiveStatus() {
        assertThatCode(() -> validator.validate(ProjectStatus.EM_ANDAMENTO, ProjectStatus.CANCELADO))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject transition that skips steps")
    void shouldRejectSkippedTransition() {
        assertThatThrownBy(() -> validator.validate(ProjectStatus.EM_ANALISE, ProjectStatus.INICIADO))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Transição inválida");
    }

    @Test
    @DisplayName("Should reject transition to the same status")
    void shouldRejectSameStatus() {
        assertThatThrownBy(() -> validator.validate(ProjectStatus.EM_ANALISE, ProjectStatus.EM_ANALISE))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("já está no status");
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"ENCERRADO", "CANCELADO"})
    @DisplayName("Should reject transition from final status")
    void shouldRejectTransitionFromFinalStatus(ProjectStatus status) {
        assertThatThrownBy(() -> validator.validate(status, ProjectStatus.EM_ANALISE))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("Should validate full sequence up to closed")
    void shouldValidateFullSequence() {
        ProjectStatus[] sequence = {
                ProjectStatus.EM_ANALISE,
                ProjectStatus.ANALISE_REALIZADA,
                ProjectStatus.ANALISE_APROVADA,
                ProjectStatus.INICIADO,
                ProjectStatus.PLANEJADO,
                ProjectStatus.EM_ANDAMENTO,
                ProjectStatus.ENCERRADO
        };

        for (int i = 0; i < sequence.length - 1; i++) {
            ProjectStatus current = sequence[i];
            ProjectStatus next = sequence[i + 1];
            assertThatCode(() -> validator.validate(current, next)).doesNotThrowAnyException();
        }
    }
}
