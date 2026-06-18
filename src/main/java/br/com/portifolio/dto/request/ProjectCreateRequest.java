package br.com.portifolio.dto.request;

import br.com.portifolio.enums.ProjectStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProjectCreateRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDate dataInicio;

    @NotNull(message = "Previsão de término é obrigatória")
    private LocalDate previsaoTermino;

    private LocalDate dataRealTermino;

    @NotNull(message = "Orçamento total é obrigatório")
    @DecimalMin(value = "0.01", message = "Orçamento deve ser maior que zero")
    private BigDecimal orcamentoTotal;

    private String descricao;

    @NotNull(message = "Gerente é obrigatório")
    private Long gerenteId;
}
