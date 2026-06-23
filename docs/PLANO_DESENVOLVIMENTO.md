# Plano de Desenvolvimento — Sistema de Portfólio de Projetos

## Visão Geral

Sistema para gerenciar o portfólio de projetos de uma empresa, com acompanhamento completo do ciclo de vida, gerenciamento de equipe, orçamento e classificação de risco.

**Stack:** Java 21, Spring Boot 3.x, Maven, PostgreSQL, JPA/Hibernate, MapStruct, Spring Security, SpringDoc OpenAPI.

**Restrição:** nenhum nome de pasta, arquivo ou código pode conter a palavra `Desbravador`.

---

## Mapeamento de Requisitos

| # | Requisito | Componente | Status |
|---|-----------|------------|--------|
| 1 | CRUD completo de projetos | `ProjectController`, `ProjectService` | Fase 3 |
| 2 | Campos: nome, datas, orçamento, descrição, gerente, status | Entity `Project` | Fase 1 |
| 3 | Classificação de risco dinâmica | `RiskClassificationService` | Fase 2 |
| 4 | Status fixos com transição sequencial | `ProjectStatusTransitionValidator` | Fase 2 |
| 5 | Cancelamento a qualquer momento | `ProjectStatusTransitionValidator` | Fase 2 |
| 6 | Bloqueio de exclusão (iniciado/andamento/encerrado) | `ProjectService.delete()` | Fase 3 |
| 7 | Membros via API externa mockada | `ExternalMemberController`, `MemberExternalClient` | Fase 4 |
| 8 | Associar membros (só funcionários) | `ProjectMemberService` | Fase 5 |
| 9 | 1–10 membros por projeto | `ProjectAllocationValidator` | Fase 2/5 |
| 10 | Máx. 3 projetos ativos por membro | `ProjectAllocationValidator` | Fase 2/5 |
| 11 | Relatório resumido do portfólio | `PortfolioReportService` | Fase 6 |
| 12 | Paginação e filtros | `ProjectSpecification` | Fase 3 |
| 13 | Spring Security (JWT) | `SecurityConfig`, `JwtService` | Fase 7/9 |
| 14 | Swagger/OpenAPI | `OpenApiConfig` | Fase 7 |
| 15 | Tratamento global de exceções | `GlobalExceptionHandler` | Fase 1 |
| 16 | Testes ≥90% camada service | JaCoCo + JUnit 5 | Fase 8 |

---

## Regras de Negócio

### Classificação de Risco

Prioridade: **ALTO > MEDIO > BAIXO** (aplica-se o maior nível que corresponder).

| Nível | Condição |
|-------|----------|
| BAIXO | orçamento ≤ R$ 100.000 **e** prazo ≤ 3 meses |
| MEDIO | orçamento entre R$ 100.001–500.000 **ou** prazo entre 3 e 6 meses |
| ALTO | orçamento > R$ 500.000 **ou** prazo > 6 meses |

Prazo = meses calendário entre `dataInicio` e `previsaoTermino` (`Period.between`).

### Fluxo de Status

```
EM_ANALISE → ANALISE_REALIZADA → ANALISE_APROVADA → INICIADO → PLANEJADO → EM_ANDAMENTO → ENCERRADO
```

- Transições apenas para o **próximo** status na sequência.
- `CANCELADO` pode ser aplicado de qualquer status ativo (exceto `ENCERRADO` e `CANCELADO`).
- Exclusão bloqueada se status ∈ `{INICIADO, EM_ANDAMENTO, ENCERRADO}`.

### Membros e Alocação

- Cadastro de membros **somente** via API externa mockada (`/api/external/members`).
- Membros sincronizados localmente com `externalId`.
- Gerente: membro com atribuição `GERENTE`.
- Equipe: apenas membros com atribuição `FUNCIONARIO`.
- Cada projeto: mínimo 1, máximo 10 membros alocados.
- Cada membro: máximo 3 projetos com status ≠ `ENCERRADO`/`CANCELADO`.

---

## Fases de Desenvolvimento

### Fase 0 — Setup
**Entregue em:** 22/06/2026 (Parte 1)
- [x] Scaffold Spring Boot Java 21 + Maven
- [x] Docker Compose PostgreSQL
- [x] Documentação (este arquivo + ARQUITETURA.md)
- [x] README com instruções

### Fase 1 — Fundação
**Entregue em:** 22/06/2026 (Parte 1)
- [x] Entities: `Project`, `Member`, `ProjectMember`
- [x] Enums: `ProjectStatus`, `MemberRole`, `RiskLevel`
- [x] Repositories JPA
- [x] Global exception handler + DTOs de erro
- [x] MapStruct mappers base

### Fase 2 — Regras de Negócio
**Entregue em:** 22/06/2026 (Parte 1)
- [x] `RiskClassificationService` + testes unitários
- [x] `ProjectStatusTransitionValidator` + testes unitários
- [x] `ProjectAllocationValidator` + testes unitários

### Fase 3 — CRUD de Projetos
**Entregue em:** 22/06/2026 (Parte 2)
- [x] Endpoints CRUD completos
- [x] Paginação + filtros (status, nome, gerente, datas, risco)
- [x] Transição de status via PATCH
- [x] Bloqueio de exclusão

### Fase 4 — Membros e API Externa
**Entregue em:** 22/06/2026 (Parte 2)
- [x] `ExternalMemberController` (API externa simulada)
- [x] `MemberExternalClient` (WebClient)
- [x] `MemberSyncService` (sincronização local)
- [x] Validação de gerente na criação/edição

### Fase 5 — Alocação de Equipe
**Entregue em:** 23/06/2026
- [x] Endpoints de alocação/remoção de membros
- [x] Validações 1–10 membros e máx. 3 projetos ativos

### Fase 6 — Relatório
**Entregue em:** 23/06/2026
- [x] Endpoint `/api/reports/portfolio`
- [x] Quantidade por status, total orçado, média duração encerrados, membros únicos

### Fase 7 — Segurança e Documentação
**Entregue em:** 23/06/2026
- [x] Spring Security (JWT stateless, usuário in-memory)
- [x] SpringDoc OpenAPI / Swagger UI

### Fase 8 — Qualidade
**Entregue em:** 23/06/2026
- [x] Testes unitários (61 testes)
- [x] JaCoCo: cobertura ≥90% em `service*`
- [x] README e documentação

### Fase 9 — Infraestrutura e Interface Web
**Entregue em:** 23/06/2026
- [x] Perfil `local` com H2 (sem Docker)
- [x] Undertow + HikariCP + WebClient com pool
- [x] Interface web (`index.html`, `app.html`) com login e dashboard
- [x] Migração de autenticação para JWT stateless

---

## Cronograma de Execução

Entregas realizadas em **2 dias**, com PRs sequenciais no GitHub:

| Data | Parte | Fases | Entregáveis |
|------|-------|-------|-------------|
| **22/06/2026** | Parte 1 | 0, 1, 2 | Setup, entities, repositórios, DTOs, validators e testes de regras de negócio |
| **22/06/2026** | Parte 2 | 3, 4 | CRUD de projetos (paginação/filtros), API externa de membros e sincronização |
| **23/06/2026** | Parte 3 | 5, 6 | Alocação de equipe e relatório do portfólio |
| **23/06/2026** | Parte 4 | 7, 8 | Spring Security, Swagger, testes expandidos e JaCoCo 90% |
| **23/06/2026** | Parte 5 | 9 | H2 local, Undertow, UI web, login JWT |

### Referência — cronograma original (5 dias)

| Dia | Fases | Entregáveis |
|-----|-------|-------------|
| 1 | 0, 1, 2 | Setup, entities, regras de negócio testadas |
| 2 | 3, 4 | CRUD projetos, mock API membros |
| 3 | 5, 6 | Alocação de equipe, relatório |
| 4 | 7, 8 | Security, Swagger, cobertura de testes |
| 5 | 9 | Infra local, UI web, JWT |

---

## Critérios de Avaliação Atendidos

- [x] Arquitetura MVC
- [x] Spring Boot
- [x] JPA + Hibernate
- [x] PostgreSQL
- [x] Clean Code e SOLID
- [x] DTOs e MapStruct
- [x] Swagger/OpenAPI
- [x] Tratamento global de exceções
- [x] Testes unitários (≥90% camada service)
- [x] Camadas controller/service/repository
- [x] Paginação e filtros
- [x] Spring Security (JWT)
