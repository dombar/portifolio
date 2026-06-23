# Portfolio Manager

Sistema para gerenciamento de portfólio de projetos, desenvolvido como solução para acompanhamento completo do ciclo de vida de projetos.

## Stack

- Java 21
- Spring Boot 3.5.15
- Spring Data JPA + Hibernate
- PostgreSQL 16
- MapStruct
- Spring Security
- SpringDoc OpenAPI (Swagger)
- Maven

## Pré-requisitos

- JDK 21
- Maven 3.9+ (ou use o Maven Wrapper `mvnw.cmd`)
- Docker e Docker Compose

## Como executar

### 1. Subir o banco de dados

```bash
docker compose up -d
```

### 2. Executar a aplicação

```bash
mvnw.cmd spring-boot:run
```

A aplicação inicia em `http://localhost:8080`.

### Documentação da API

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Autenticação

Todos os endpoints (exceto `/api/external/**` e Swagger) exigem HTTP Basic Auth:

| Usuário | Senha |
|---------|-------|
| admin   | admin123 |

## Fluxo de uso

### 1. Criar membros na API externa (mock)

```bash
curl -X POST http://localhost:8080/api/external/members \
  -H "Content-Type: application/json" \
  -d '{"nome": "João Gerente", "atribuicao": "GERENTE"}'

curl -X POST http://localhost:8080/api/external/members \
  -H "Content-Type: application/json" \
  -d '{"nome": "Maria Dev", "atribuicao": "FUNCIONARIO"}'
```

### 2. Sincronizar membros

```bash
curl -X POST http://localhost:8080/api/members/sync/1 -u admin:admin123
curl -X POST http://localhost:8080/api/members/sync/2 -u admin:admin123
```

### 3. Criar projeto

```bash
curl -X POST http://localhost:8080/api/projects \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Projeto Alpha",
    "dataInicio": "2026-01-01",
    "previsaoTermino": "2026-04-01",
    "orcamentoTotal": 80000.00,
    "descricao": "Projeto piloto",
    "gerenteId": 1
  }'
```

### 4. Alocar membros

```bash
curl -X POST http://localhost:8080/api/projects/1/members \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"memberId": 2}'
```

### 5. Consultar relatório

```bash
curl http://localhost:8080/api/reports/portfolio -u admin:admin123
```

## Testes

```bash
mvnw.cmd test
```

Verificar cobertura JaCoCo:

```bash
mvnw.cmd verify
```

Relatório em `target/site/jacoco/index.html`.

## Documentação

- [Plano de Desenvolvimento](docs/PLANO_DESENVOLVIMENTO.md)
- [Arquitetura](docs/ARQUITETURA.md)
