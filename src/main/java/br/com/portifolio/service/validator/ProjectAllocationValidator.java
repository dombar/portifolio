package br.com.portifolio.service.validator;

import br.com.portifolio.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

@Component
public class ProjectAllocationValidator {

    public static final int MIN_MEMBERS        = 1;
    public static final int MAX_MEMBERS        = 10;
    public static final int MAX_ACTIVE_PROJECTS = 3;

    public void validateMemberCountForAllocation(long currentCount) {
        guard(currentCount >= MAX_MEMBERS,
                "Projeto já possui o máximo de " + MAX_MEMBERS + " membros alocados");
    }

    public void validateMemberCountForRemoval(long currentCount) {
        guard(currentCount <= MIN_MEMBERS,
                "Projeto deve manter no mínimo " + MIN_MEMBERS + " membro alocado");
    }

    public void validateActiveProjectsLimit(long activeProjectCount) {
        guard(activeProjectCount >= MAX_ACTIVE_PROJECTS,
                "Membro já está alocado em " + MAX_ACTIVE_PROJECTS + " projetos ativos");
    }

    private void guard(boolean violated, String message) {
        if (violated) throw new BusinessRuleException(message);
    }
}
