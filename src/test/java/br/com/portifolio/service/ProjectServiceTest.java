package br.com.portifolio.service;

import br.com.portifolio.dto.request.ProjectCreateRequest;
import br.com.portifolio.dto.request.ProjectStatusUpdateRequest;
import br.com.portifolio.dto.request.ProjectUpdateRequest;
import br.com.portifolio.dto.response.ProjectResponse;
import br.com.portifolio.entity.Member;
import br.com.portifolio.entity.Project;
import br.com.portifolio.enums.MemberRole;
import br.com.portifolio.enums.ProjectStatus;
import br.com.portifolio.enums.RiskLevel;
import br.com.portifolio.exception.BusinessRuleException;
import br.com.portifolio.exception.ResourceNotFoundException;
import br.com.portifolio.mapper.ProjectMapper;
import br.com.portifolio.repository.ProjectRepository;
import br.com.portifolio.service.validator.ProjectStatusTransitionValidator;
import br.com.portifolio.service.validator.RiskClassificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MemberSyncService memberSyncService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private RiskClassificationService riskClassificationService;

    @Mock
    private ProjectStatusTransitionValidator statusTransitionValidator;

    @InjectMocks
    private ProjectService projectService;

    private Member gerente;
    private Project project;

    @BeforeEach
    void setUp() {
        gerente = Member.builder()
                .id(1L)
                .externalId(1L)
                .nome("Gerente")
                .atribuicao(MemberRole.GERENTE)
                .build();

        project = Project.builder()
                .id(1L)
                .nome("Projeto Teste")
                .dataInicio(LocalDate.of(2026, 1, 1))
                .previsaoTermino(LocalDate.of(2026, 4, 1))
                .orcamentoTotal(new BigDecimal("80000"))
                .gerente(gerente)
                .status(ProjectStatus.EM_ANALISE)
                .build();
    }

    @Test
    @DisplayName("Should create project successfully")
    void shouldCreateProject() {
        ProjectCreateRequest request = buildCreateRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 1), 1L);

        when(memberSyncService.getMemberEntity(1L)).thenReturn(gerente);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(riskClassificationService.classify(any(), any(), any())).thenReturn(RiskLevel.BAIXO);
        when(projectMapper.toResponseWithRisk(any(), any())).thenReturn(
                ProjectResponse.builder().id(1L).nome("Projeto Teste").nivelRisco(RiskLevel.BAIXO).build());

        ProjectResponse result = projectService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("Should reject creation when end date is before start date")
    void shouldRejectCreateWhenEndBeforeStart() {
        ProjectCreateRequest request = buildCreateRequest(
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 1, 1), 1L);

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    @DisplayName("Should reject creation when manager does not have GERENTE role")
    void shouldRejectNonManagerAsGerente() {
        Member funcionario = Member.builder().id(2L).atribuicao(MemberRole.FUNCIONARIO).build();
        ProjectCreateRequest request = buildCreateRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 1), 2L);

        when(memberSyncService.getMemberEntity(2L)).thenReturn(funcionario);

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("GERENTE");
    }

    @Test
    @DisplayName("Should return project by ID with calculated risk")
    void shouldFindProjectById() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(riskClassificationService.classify(any(), any(), any())).thenReturn(RiskLevel.BAIXO);
        when(projectMapper.toResponseWithRisk(any(), any())).thenReturn(
                ProjectResponse.builder().id(1L).build());

        ProjectResponse result = projectService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw exception when project is not found")
    void shouldThrowWhenProjectNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should list projects without risk filter")
    @SuppressWarnings("unchecked")
    void shouldFindAllWithoutRiskFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(List.of(project), pageable, 1);

        when(projectRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(riskClassificationService.classify(any(), any(), any())).thenReturn(RiskLevel.BAIXO);
        when(projectMapper.toResponseWithRisk(any(), any())).thenReturn(
                ProjectResponse.builder().id(1L).nivelRisco(RiskLevel.BAIXO).build());

        Page<ProjectResponse> result = projectService.findAll(
                null, null, null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should filter projects by risk level")
    @SuppressWarnings("unchecked")
    void shouldFindAllFilteredByRiskLevel() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(List.of(project), pageable, 1);

        when(projectRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(riskClassificationService.classify(any(), any(), any())).thenReturn(RiskLevel.BAIXO);
        when(projectMapper.toResponseWithRisk(any(), any())).thenReturn(
                ProjectResponse.builder().id(1L).nivelRisco(RiskLevel.BAIXO).build());

        Page<ProjectResponse> result = projectService.findAll(
                null, null, null, null, null, RiskLevel.BAIXO, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNivelRisco()).isEqualTo(RiskLevel.BAIXO);
    }

    @Test
    @DisplayName("Should return empty list when no project matches risk filter")
    @SuppressWarnings("unchecked")
    void shouldReturnEmptyWhenNoProjectMatchesRiskFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(List.of(project), pageable, 1);

        when(projectRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(riskClassificationService.classify(any(), any(), any())).thenReturn(RiskLevel.BAIXO);
        when(projectMapper.toResponseWithRisk(any(), any())).thenReturn(
                ProjectResponse.builder().id(1L).nivelRisco(RiskLevel.BAIXO).build());

        Page<ProjectResponse> result = projectService.findAll(
                null, null, null, null, null, RiskLevel.ALTO, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should update project successfully")
    void shouldUpdateProject() {
        ProjectUpdateRequest request = buildUpdateRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), 1L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(memberSyncService.getMemberEntity(1L)).thenReturn(gerente);
        when(projectRepository.save(any())).thenReturn(project);
        when(riskClassificationService.classify(any(), any(), any())).thenReturn(RiskLevel.MEDIO);
        when(projectMapper.toResponseWithRisk(any(), any())).thenReturn(
                ProjectResponse.builder().id(1L).nome("Projeto Atualizado").build());

        ProjectResponse result = projectService.update(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("Should reject update when end date is before start date")
    void shouldRejectUpdateWhenEndBeforeStart() {
        ProjectUpdateRequest request = buildUpdateRequest(
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 1, 1), 1L);

        assertThatThrownBy(() -> projectService.update(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    @DisplayName("Should reject update with invalid manager")
    void shouldRejectUpdateWithInvalidGerente() {
        Member funcionario = Member.builder().id(2L).atribuicao(MemberRole.FUNCIONARIO).build();
        ProjectUpdateRequest request = buildUpdateRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 1), 2L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(memberSyncService.getMemberEntity(2L)).thenReturn(funcionario);

        assertThatThrownBy(() -> projectService.update(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("GERENTE");
    }

    @Test
    @DisplayName("Should set actual end date when closing project without existing date")
    void shouldSetRealEndDateWhenClosingWithoutExistingDate() {
        project.setStatus(ProjectStatus.EM_ANDAMENTO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(riskClassificationService.classify(any(), any(), any())).thenReturn(RiskLevel.BAIXO);
        when(projectMapper.toResponseWithRisk(any(), any())).thenReturn(
                ProjectResponse.builder().status(ProjectStatus.ENCERRADO).build());

        ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest();
        request.setStatus(ProjectStatus.ENCERRADO);

        projectService.updateStatus(1L, request);

        verify(projectRepository).save(argThat(p ->
                p.getStatus() == ProjectStatus.ENCERRADO && p.getDataRealTermino() != null));
    }

    @Test
    @DisplayName("Should not overwrite existing actual end date when closing")
    void shouldNotOverwriteExistingRealEndDateWhenClosing() {
        LocalDate existingDate = LocalDate.of(2026, 3, 15);
        project.setStatus(ProjectStatus.EM_ANDAMENTO);
        project.setDataRealTermino(existingDate);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(riskClassificationService.classify(any(), any(), any())).thenReturn(RiskLevel.BAIXO);
        when(projectMapper.toResponseWithRisk(any(), any())).thenReturn(
                ProjectResponse.builder().status(ProjectStatus.ENCERRADO).build());

        ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest();
        request.setStatus(ProjectStatus.ENCERRADO);

        projectService.updateStatus(1L, request);

        verify(projectRepository).save(argThat(p ->
                p.getDataRealTermino().isEqual(existingDate)));
    }

    @Test
    @DisplayName("Should update status without setting end date for non-ENCERRADO status")
    void shouldUpdateStatusWithoutSettingEndDate() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(riskClassificationService.classify(any(), any(), any())).thenReturn(RiskLevel.BAIXO);
        when(projectMapper.toResponseWithRisk(any(), any())).thenReturn(
                ProjectResponse.builder().status(ProjectStatus.ANALISE_REALIZADA).build());

        ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest();
        request.setStatus(ProjectStatus.ANALISE_REALIZADA);

        projectService.updateStatus(1L, request);

        verify(projectRepository).save(argThat(p ->
                p.getStatus() == ProjectStatus.ANALISE_REALIZADA && p.getDataRealTermino() == null));
    }

    @Test
    @DisplayName("Should block deletion of in-progress project")
    void shouldBlockDeletionOfActiveProject() {
        project.setStatus(ProjectStatus.EM_ANDAMENTO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.delete(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não pode ser excluído");
    }

    @Test
    @DisplayName("Should block deletion of closed project")
    void shouldBlockDeletionOfClosedProject() {
        project.setStatus(ProjectStatus.ENCERRADO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.delete(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não pode ser excluído");
    }

    @Test
    @DisplayName("Should block deletion of started project")
    void shouldBlockDeletionOfStartedProject() {
        project.setStatus(ProjectStatus.INICIADO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.delete(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não pode ser excluído");
    }

    @Test
    @DisplayName("Should allow deletion of project under analysis")
    void shouldAllowDeletionOfAnalysisProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.delete(1L);

        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("Should allow deletion of cancelled project")
    void shouldAllowDeletionOfCancelledProject() {
        project.setStatus(ProjectStatus.CANCELADO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.delete(1L);

        verify(projectRepository).delete(project);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"ANALISE_REALIZADA", "ANALISE_APROVADA", "PLANEJADO"})
    @DisplayName("Should allow deletion of project with non-blocked statuses")
    void shouldAllowDeletionOfNonBlockedStatuses(ProjectStatus status) {
        project.setStatus(status);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.delete(1L);

        verify(projectRepository).delete(project);
        clearInvocations(projectRepository);
    }

    @Test
    @DisplayName("Should return project when searching by existing ID")
    void shouldReturnProjectByExistingId() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project result = projectService.getProjectOrThrow(1L);

        assertThat(result).isEqualTo(project);
    }

    private ProjectCreateRequest buildCreateRequest(LocalDate inicio, LocalDate termino, Long gerenteId) {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setNome("Projeto");
        request.setDataInicio(inicio);
        request.setPrevisaoTermino(termino);
        request.setOrcamentoTotal(new BigDecimal("80000"));
        request.setGerenteId(gerenteId);
        return request;
    }

    private ProjectUpdateRequest buildUpdateRequest(LocalDate inicio, LocalDate termino, Long gerenteId) {
        ProjectUpdateRequest request = new ProjectUpdateRequest();
        request.setNome("Projeto Atualizado");
        request.setDataInicio(inicio);
        request.setPrevisaoTermino(termino);
        request.setOrcamentoTotal(new BigDecimal("80000"));
        request.setGerenteId(gerenteId);
        return request;
    }
}
