package br.com.portifolio.repository;

import br.com.portifolio.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectId(Long projectId);

    Optional<ProjectMember> findByProjectIdAndMemberId(Long projectId, Long memberId);

    long countByProjectId(Long projectId);

    boolean existsByProjectIdAndMemberId(Long projectId, Long memberId);

    @Query("SELECT COUNT(DISTINCT pm.member.id) FROM ProjectMember pm")
    long countDistinctMembers();
}
