package br.com.portifolio.dto.request;

import br.com.portifolio.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectStatusUpdateRequest {

    @NotNull(message = "Status é obrigatório")
    private ProjectStatus status;
}
