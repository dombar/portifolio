package br.com.portifolio.service;

import br.com.portifolio.dto.response.MemberResponse;
import br.com.portifolio.entity.Member;
import br.com.portifolio.entity.Project;
import br.com.portifolio.entity.ProjectMember;
import br.com.portifolio.enums.MemberRole;
import br.com.portifolio.enums.ProjectStatus;
import br.com.portifolio.exception.BusinessRuleException;
import br.com.portifolio.exception.ResourceNotFoundException;
import br.com.portifolio.mapper.MemberMapper;
import br.com.portifolio.repository.ProjectMemberRepository;
import br.com.portifolio.repository.ProjectRepository;
import br.com.portifolio.service.validator.ProjectAllocationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private MemberSyncService memberSyncService;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private ProjectAllocationValidator allocationValidator;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    private Project project;
    private Project activeProject;
    private Member funcionario;
    private Member gerente;
    private ProjectMember allocation;

    @BeforeEach
    void setUp() {
        gerente = Member.builder().id(1L).atribuicao(MemberRole.GERENTE).build();
        funcionario = Member.builder().id(2L).atribuicao(MemberRole.FUNCIONARIO).nome("Dev").build();
        project = Project.builder().id(1L).status(ProjectStatus.EM_ANALISE).gerente(gerente).build();
        activeProject = Project.builder().id(2L).status(ProjectStatus.EM_ANDAMENTO).gerente(gerente).build();
        allocation = ProjectMember.builder().project(project).member(funcionario).build();
    }

    @Test
    @DisplayName("Should list members of a project successfully")
    void shouldListMembersSuccessfully() {
        when(projectService.getProjectOrThrow(1L)).thenReturn(project);
        when(projectMemberRepository.findByProjectId(1L)).thenReturn(List.of(allocation));
        when(memberMapper.toResponse(funcionario)).thenReturn(
                MemberResponse.builder().id(2L).nome("Dev").atribuicao(MemberRole.FUNCIONARIO).build());

        List<MemberResponse> result = projectMemberService.listMembers(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should return empty list when project has no members")
    void shouldReturnEmptyListWhenNoMembers() {
        when(projectService.getProjectOrThrow(1L)).thenReturn(project);
        when(projectMemberRepository.findByProjectId(1L)).thenReturn(List.of());

        List<MemberResponse> result = projectMemberService.listMembers(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should reject allocation of member without FUNCIONARIO role")
    void shouldRejectNonEmployeeAllocation() {
        Member gerenteMember = Member.builder().id(3L).atribuicao(MemberRole.GERENTE).build();

        when(projectService.getProjectOrThrow(1L)).thenReturn(project);
        when(memberSyncService.getMemberEntity(3L)).thenReturn(gerenteMember);

        assertThatThrownBy(() -> projectMemberService.allocateMember(1L, 3L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("FUNCIONARIO");
    }

    @Test
    @DisplayName("Should reject duplicate allocation")
    void shouldRejectDuplicateAllocation() {
        when(projectService.getProjectOrThrow(1L)).thenReturn(project);
        when(memberSyncService.getMemberEntity(2L)).thenReturn(funcionario);
        when(projectMemberRepository.existsByProjectIdAndMemberId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> projectMemberService.allocateMember(1L, 2L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("já está alocado");
    }

    @Test
    @DisplayName("Should allocate employee successfully in inactive project without active limit check")
    void shouldAllocateEmployeeSuccessfullyInInactiveProject() {
        Project encerrado = Project.builder().id(3L).status(ProjectStatus.ENCERRADO).gerente(gerente).build();

        when(projectService.getProjectOrThrow(3L)).thenReturn(encerrado);
        when(memberSyncService.getMemberEntity(2L)).thenReturn(funcionario);
        when(projectMemberRepository.existsByProjectIdAndMemberId(3L, 2L)).thenReturn(false);
        when(projectMemberRepository.countByProjectId(3L)).thenReturn(0L);
        when(projectMemberRepository.save(any())).thenAnswer(inv -> {
            ProjectMember pm = inv.getArgument(0);
            return ProjectMember.builder().project(pm.getProject()).member(funcionario).build();
        });
        when(memberMapper.toResponse(any())).thenReturn(
                MemberResponse.builder().id(2L).nome("Dev").atribuicao(MemberRole.FUNCIONARIO).build());

        MemberResponse result = projectMemberService.allocateMember(3L, 2L);

        assertThat(result.getId()).isEqualTo(2L);
        verify(projectMemberRepository).save(any(ProjectMember.class));
        verify(allocationValidator).validateMemberCountForAllocation(0L);
        verify(projectRepository, never()).countActiveProjectsByMemberId(any());
    }

    @Test
    @DisplayName("Should check active projects limit when allocating in active project")
    void shouldCheckActiveLimitWhenAllocatingInActiveProject() {
        when(projectService.getProjectOrThrow(2L)).thenReturn(activeProject);
        when(memberSyncService.getMemberEntity(2L)).thenReturn(funcionario);
        when(projectMemberRepository.existsByProjectIdAndMemberId(2L, 2L)).thenReturn(false);
        when(projectMemberRepository.countByProjectId(2L)).thenReturn(1L);
        when(projectRepository.countActiveProjectsByMemberId(2L)).thenReturn(1L);
        when(projectMemberRepository.save(any())).thenAnswer(inv -> {
            ProjectMember pm = inv.getArgument(0);
            return ProjectMember.builder().project(pm.getProject()).member(funcionario).build();
        });
        when(memberMapper.toResponse(any())).thenReturn(
                MemberResponse.builder().id(2L).nome("Dev").atribuicao(MemberRole.FUNCIONARIO).build());

        projectMemberService.allocateMember(2L, 2L);

        verify(allocationValidator).validateActiveProjectsLimit(1L);
    }

    @Test
    @DisplayName("Should remove member from project successfully")
    void shouldRemoveMemberSuccessfully() {
        when(projectService.getProjectOrThrow(1L)).thenReturn(project);
        when(projectMemberRepository.findByProjectIdAndMemberId(1L, 2L))
                .thenReturn(Optional.of(allocation));
        when(projectMemberRepository.countByProjectId(1L)).thenReturn(3L);

        projectMemberService.removeMember(1L, 2L);

        verify(projectMemberRepository).delete(allocation);
        verify(allocationValidator).validateMemberCountForRemoval(3L);
    }

    @Test
    @DisplayName("Should throw exception when removing non-allocated member")
    void shouldThrowWhenRemovingNonAllocatedMember() {
        when(projectService.getProjectOrThrow(1L)).thenReturn(project);
        when(projectMemberRepository.findByProjectIdAndMemberId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectMemberService.removeMember(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
