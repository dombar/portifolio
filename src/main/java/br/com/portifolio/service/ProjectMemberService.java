package br.com.portifolio.service;

import br.com.portifolio.dto.response.MemberResponse;
import br.com.portifolio.entity.Member;
import br.com.portifolio.entity.Project;
import br.com.portifolio.entity.ProjectMember;
import br.com.portifolio.enums.MemberRole;
import br.com.portifolio.exception.BusinessRuleException;
import br.com.portifolio.exception.ResourceNotFoundException;
import br.com.portifolio.mapper.MemberMapper;
import br.com.portifolio.repository.ProjectMemberRepository;
import br.com.portifolio.repository.ProjectRepository;
import br.com.portifolio.service.validator.ProjectAllocationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final MemberSyncService memberSyncService;
    private final MemberMapper memberMapper;
    private final ProjectAllocationValidator allocationValidator;

    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(Long projectId) {
        projectService.getProjectOrThrow(projectId);
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(pm -> memberMapper.toResponse(pm.getMember()))
                .toList();
    }

    @Transactional
    public MemberResponse allocateMember(Long projectId, Long memberId) {
        Project project = projectService.getProjectOrThrow(projectId);
        Member member = memberSyncService.getMemberEntity(memberId);

        if (member.getAtribuicao() != MemberRole.FUNCIONARIO) {
            throw new BusinessRuleException("Apenas membros com atribuição FUNCIONARIO podem ser alocados");
        }

        if (projectMemberRepository.existsByProjectIdAndMemberId(projectId, memberId)) {
            throw new BusinessRuleException("Membro já está alocado neste projeto");
        }

        long currentCount = projectMemberRepository.countByProjectId(projectId);
        allocationValidator.validateMemberCountForAllocation(currentCount);

        if (project.getStatus().isActive()) {
            long activeProjects = projectRepository.countActiveProjectsByMemberId(memberId);
            allocationValidator.validateActiveProjectsLimit(activeProjects);
        }

        ProjectMember allocation = ProjectMember.builder()
                .project(project)
                .member(member)
                .build();

        return memberMapper.toResponse(projectMemberRepository.save(allocation).getMember());
    }

    @Transactional
    public void removeMember(Long projectId, Long memberId) {
        projectService.getProjectOrThrow(projectId);

        ProjectMember allocation = projectMemberRepository
                .findByProjectIdAndMemberId(projectId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membro " + memberId + " não está alocado no projeto " + projectId));

        long currentCount = projectMemberRepository.countByProjectId(projectId);
        allocationValidator.validateMemberCountForRemoval(currentCount);

        projectMemberRepository.delete(allocation);
    }
}
