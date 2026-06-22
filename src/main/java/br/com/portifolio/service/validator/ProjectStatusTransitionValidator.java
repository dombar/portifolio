package br.com.portifolio.service.validator;

import br.com.portifolio.enums.ProjectStatus;
import br.com.portifolio.exception.InvalidStatusTransitionException;
import org.springframework.stereotype.Component;

@Component
public class ProjectStatusTransitionValidator {

    public void validate(ProjectStatus current, ProjectStatus target) {
        rejectIfSame(current, target);
        rejectIfTerminal(current);
        if (target != ProjectStatus.CANCELADO) {
            requireSequentialTransition(current, target);
        }
    }

    private void rejectIfSame(ProjectStatus current, ProjectStatus target) {
        if (current == target) {
            throw new InvalidStatusTransitionException(
                    "O projeto já está no status " + current.getDisplayName());
        }
    }

    private void rejectIfTerminal(ProjectStatus current) {
        if (!current.isActive()) {
            throw new InvalidStatusTransitionException(
                    "Não é possível alterar o status de um projeto " + current.getDisplayName());
        }
    }

    private void requireSequentialTransition(ProjectStatus current, ProjectStatus target) {
        current.next()
                .filter(expected -> expected == target)
                .orElseThrow(() -> new InvalidStatusTransitionException(
                        "Transição inválida de " + current.getDisplayName()
                        + " para " + target.getDisplayName()
                        + ". Próximo status esperado: "
                        + current.next().map(ProjectStatus::getDisplayName).orElse("nenhum")));
    }
}
