package br.com.portifolio.enums;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public enum ProjectStatus {
    EM_ANALISE,
    ANALISE_REALIZADA,
    ANALISE_APROVADA,
    INICIADO,
    PLANEJADO,
    EM_ANDAMENTO,
    ENCERRADO,
    CANCELADO;

    private static final Set<ProjectStatus> TERMINAIS        = EnumSet.of(ENCERRADO, CANCELADO);
    private static final Set<ProjectStatus> BLOQUEIAM_DELETE = EnumSet.of(INICIADO, EM_ANDAMENTO, ENCERRADO);

    public boolean isActive() {
        return !TERMINAIS.contains(this);
    }

    public boolean isDeletionBlocked() {
        return BLOQUEIAM_DELETE.contains(this);
    }

    public String getDisplayName() {
        return name().toLowerCase().replace('_', ' ');
    }

    public Optional<ProjectStatus> next() {
        return Optional.ofNullable(switch (this) {
            case EM_ANALISE       -> ANALISE_REALIZADA;
            case ANALISE_REALIZADA -> ANALISE_APROVADA;
            case ANALISE_APROVADA  -> INICIADO;
            case INICIADO          -> PLANEJADO;
            case PLANEJADO         -> EM_ANDAMENTO;
            case EM_ANDAMENTO      -> ENCERRADO;
            default                -> null;
        });
    }
}
