package br.com.portifolio.mapper;

import br.com.portifolio.dto.response.ProjectResponse;
import br.com.portifolio.entity.Project;
import br.com.portifolio.enums.RiskLevel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "gerenteId",   source = "gerente.id")
    @Mapping(target = "gerenteNome", source = "gerente.nome")
    @Mapping(target = "nivelRisco",  ignore = true)
    ProjectResponse toResponse(Project project);

    @Mapping(target = "gerenteId",   source = "project.gerente.id")
    @Mapping(target = "gerenteNome", source = "project.gerente.nome")
    @Mapping(target = "nivelRisco",  source = "riskLevel")
    ProjectResponse toResponseWithRisk(Project project, RiskLevel riskLevel);
}
