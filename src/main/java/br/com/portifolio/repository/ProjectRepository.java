package br.com.portifolio.repository;

import br.com.portifolio.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    @Query("SELECT p.status, COUNT(p) FROM Project p GROUP BY p.status")
    List<Object[]> countByStatus();

    @Query("SELECT p.status, SUM(p.orcamentoTotal) FROM Project p GROUP BY p.status")
    List<Object[]> sumBudgetByStatus();

    @Query(value = """
            SELECT AVG(p.data_real_termino - p.data_inicio)
            FROM projects p
            WHERE p.status = 'ENCERRADO'
            AND p.data_real_termino IS NOT NULL
            """, nativeQuery = true)
    Double averageDurationOfClosedProjects();

    @Query("""
            SELECT COUNT(DISTINCT p)
            FROM Project p
            JOIN ProjectMember pm ON pm.project = p
            WHERE pm.member.id = :memberId
            AND p.status NOT IN (br.com.portifolio.enums.ProjectStatus.ENCERRADO,
                                 br.com.portifolio.enums.ProjectStatus.CANCELADO)
            """)
    long countActiveProjectsByMemberId(@Param("memberId") Long memberId);
}
