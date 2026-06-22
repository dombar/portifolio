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
import br.com.portifolio.repository.spec.ProjectSpecification;
import br.com.portifolio.service.validator.ProjectStatusTransitionValidator;
import br.com.portifolio.service.validator.RiskClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberSyncService memberSyncService;
    private final ProjectMapper projectMapper;
    private final RiskClassificationService riskClassificationService;
    private final ProjectStatusTransitionValidator statusTransitionValidator;

    @Transactional(readOnly = true)
    public Page<ProjectResponse> findAll(
            ProjectStatus status,
            String nome,
            Long gerenteId,
            LocalDate dataInicioDe,
            LocalDate dataInicioAte,
            RiskLevel nivelRisco,
            Pageable pageable) {

        Specification<Project> spec = ProjectSpecification.withFilters(
                status, nome, gerenteId, dataInicioDe, dataInicioAte);

        Page<Project> page = projectRepository.findAll(spec, pageable);

        if (nivelRisco != null) {
            List<ProjectResponse> filtered = page.getContent().stream()
                    .map(this::toResponseWithRisk)
                    .filter(r -> r.getNivelRisco() == nivelRisco)
                    .toList();
            return new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
        }

        return page.map(this::toResponseWithRisk);
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id) {
        Project project = getProjectOrThrow(id);
        return toResponseWithRisk(project);
    }

    @Transactional
    public ProjectResponse create(ProjectCreateRequest request) {
        validateDates(request.getDataInicio(), request.getPrevisaoTermino());
        Member gerente = memberSyncService.getMemberEntity(request.getGerenteId());
        validateGerente(gerente);

        Project project = Project.builder()
                .nome(request.getNome())
                .dataInicio(request.getDataInicio())
                .previsaoTermino(request.getPrevisaoTermino())
                .dataRealTermino(request.getDataRealTermino())
                .orcamentoTotal(request.getOrcamentoTotal())
                .descricao(request.getDescricao())
                .gerente(gerente)
                .status(ProjectStatus.EM_ANALISE)
                .build();

        return toResponseWithRisk(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectUpdateRequest request) {
        validateDates(request.getDataInicio(), request.getPrevisaoTermino());
        Project project = getProjectOrThrow(id);
        Member gerente = memberSyncService.getMemberEntity(request.getGerenteId());
        validateGerente(gerente);

        project.setNome(request.getNome());
        project.setDataInicio(request.getDataInicio());
        project.setPrevisaoTermino(request.getPrevisaoTermino());
        project.setDataRealTermino(request.getDataRealTermino());
        project.setOrcamentoTotal(request.getOrcamentoTotal());
        project.setDescricao(request.getDescricao());
        project.setGerente(gerente);

        return toResponseWithRisk(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse updateStatus(Long id, ProjectStatusUpdateRequest request) {
        Project project = getProjectOrThrow(id);
        statusTransitionValidator.validate(project.getStatus(), request.getStatus());

        project.setStatus(request.getStatus());

        if (request.getStatus() == ProjectStatus.ENCERRADO && project.getDataRealTermino() == null) {
            project.setDataRealTermino(LocalDate.now());
        }

        return toResponseWithRisk(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id) {
        Project project = getProjectOrThrow(id);

        if (project.getStatus().isDeletionBlocked()) {
            throw new BusinessRuleException(
                    "Projeto com status '" + project.getStatus() + "' não pode ser excluído");
        }

        projectRepository.delete(project);
    }

    public Project getProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado: " + id));
    }

    private ProjectResponse toResponseWithRisk(Project project) {
        RiskLevel risk = riskClassificationService.classify(
                project.getOrcamentoTotal(),
                project.getDataInicio(),
                project.getPrevisaoTermino());
        return projectMapper.toResponseWithRisk(project, risk);
    }

    private void validateDates(LocalDate inicio, LocalDate termino) {
        if (termino.isBefore(inicio)) {
            throw new BusinessRuleException("Previsão de término deve ser posterior à data de início");
        }
    }

    private void validateGerente(Member gerente) {
        if (gerente.getAtribuicao() != MemberRole.GERENTE) {
            throw new BusinessRuleException("Gerente responsável deve possuir atribuição GERENTE");
        }
    }
}
