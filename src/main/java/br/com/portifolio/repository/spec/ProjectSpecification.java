package br.com.portifolio.repository.spec;

import br.com.portifolio.entity.Project;
import br.com.portifolio.enums.ProjectStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

public final class ProjectSpecification {

    private ProjectSpecification() {
    }

    public static Specification<Project> withFilters(
            ProjectStatus status,
            String nome,
            Long gerenteId,
            LocalDate dataInicioDe,
            LocalDate dataInicioAte) {

        return (root, query, cb) -> {
            var predicates = Stream.<Optional<Predicate>>of(
                    Optional.ofNullable(status)
                            .map(s -> cb.equal(root.get("status"), s)),
                    Optional.ofNullable(nome).filter(java.util.function.Predicate.not(String::isBlank))
                            .map(n -> cb.like(cb.lower(root.get("nome")), "%" + n.toLowerCase() + "%")),
                    Optional.ofNullable(gerenteId)
                            .map(g -> cb.equal(root.get("gerente").get("id"), g)),
                    Optional.ofNullable(dataInicioDe)
                            .map(d -> cb.greaterThanOrEqualTo(root.get("dataInicio"), d)),
                    Optional.ofNullable(dataInicioAte)
                            .map(d -> cb.lessThanOrEqualTo(root.get("dataInicio"), d))
            ).flatMap(Optional::stream).toArray(Predicate[]::new);

            return predicates.length == 0 ? cb.conjunction() : cb.and(predicates);
        };
    }
}
