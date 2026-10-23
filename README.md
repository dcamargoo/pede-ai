# Pede Aí

API REST para gerenciar pedidos de delivery de um único restaurante, desenvolvida com **Java 21** e **Spring Boot 4**.

> 🚧 **Em desenvolvimento.** Os próximos passos são autenticação (JWT), melhorias de desempenho e deploy. Veja o [Roadmap](#roadmap).

## Sobre o projeto

Restaurantes pequenos costumam receber pedidos por mensagem. O cardápio fica desatualizado, o total é calculado na mão e ninguém sabe em que etapa cada pedido está.

O **Pede Aí** resolve isso centralizando o cardápio e os pedidos em uma API:

- O **cliente** consulta o cardápio, faz pedidos e acompanha ou cancela cada um deles.
- O **restaurante** gerencia as categorias e os itens do cardápio, e avança o status de cada pedido até a entrega.

**Exemplo:** Marina pede 2 hambúrgueres (R$ 32,00 cada) e 1 refrigerante (R$ 7,00). A API calcula o total de **R$ 71,00** e cria o pedido com o status `RECEIVED`. Depois, o restaurante muda o status para `IN_PREPARATION`, `OUT_FOR_DELIVERY` e, por fim, `DELIVERED`.

## Funcionalidades

- Cadastro de clientes
- Categorias do cardápio (criar, editar, remover e listar)
- Itens do cardápio (criar, editar, remover, filtrar por categoria e disponibilidade, marcar como indisponível)
- Pedidos (criar, listar com filtro por status, consultar detalhes, cancelar e avançar o status)
- Respostas de erro padronizadas
- Versionamento do banco de dados com Flyway
- Testes unitários e de integração

## Principais regras de negócio

| Regra | Descrição |
|---|---|
| Total calculado pela API | O cliente envia apenas os itens e as quantidades. Os preços e o total sempre vêm da API. |
| Preço congelado no pedido | O preço do item é copiado para o pedido no momento da compra. Mudanças posteriores no cardápio não alteram pedidos antigos. |
| Fluxo de status | `RECEIVED → IN_PREPARATION → OUT_FOR_DELIVERY → DELIVERED`. Não é possível pular nem voltar etapas. |
| Cancelamento | Um pedido só pode ser cancelado enquanto estiver com o status `RECEIVED`. |
| Itens indisponíveis | Um item indisponível não pode entrar em um pedido novo. |
| Remoção segura | Um item que já foi pedido não pode ser removido (apenas marcado como indisponível), e uma categoria que ainda tem itens não pode ser removida. |
| Dados únicos | O email do usuário e o nome da categoria são únicos. |

## Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4 (Web MVC, Data JPA, Validation) |
| Banco de dados | PostgreSQL 16 |
| Migrations | Flyway |
| Build | Maven |
| Testes | JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers |
| Cobertura | JaCoCo |
| Outros | Lombok, SLF4J / Logback |

## Arquitetura

O projeto é um **monolito** organizado em **camadas**:

```
Controller  →  Service  →  Repository  →  PostgreSQL
(HTTP)         (regras de    (acesso aos
                negócio)      dados)
```

```
src/main/java/io/github/dcamargoo/pede_ai
├── controller   endpoints REST
├── service      regras de negócio
├── repository   repositories do Spring Data JPA
├── entity       entidades JPA
├── dto          records de requisição e de resposta
├── enums        Role, OrderStatus
└── exception    exceções personalizadas e tratamento global de erros
```

### Algumas decisões

- **DTOs como records do Java:** as entidades nunca são devolvidas pela API. Assim, dados sensíveis, como a senha do usuário, nunca saem da aplicação.
- **Tratamento global de erros:** um único `@RestControllerAdvice` transforma cada exceção no mesmo formato de erro, com o status HTTP correto.
- **Flyway + `ddl-auto: validate`:** o Flyway cria e altera o banco por meio de scripts SQL versionados. O Hibernate apenas valida se as entidades batem com as tabelas.
- **Relacionamentos `LAZY`:** os dados relacionados só são carregados quando são necessários.
- **O `PATCH /orders/{id}/advance` não recebe o novo status:** a API move o pedido para a próxima etapa, então é impossível pular etapas.

## Como executar

### Pré-requisitos

- Java 21
- PostgreSQL 16
- Docker (apenas para rodar os testes de integração)

### 1. Clone o repositório

```bash
git clone https://github.com/dcamargoo/pede-ai.git
cd pede-ai
```

### 2. Crie o banco de dados

```sql
CREATE USER pedeai WITH PASSWORD 'pedeai';
CREATE DATABASE pedeai OWNER pedeai;
```

### 3. Rode a aplicação

```bash
./mvnw spring-boot:run
```

O Flyway cria as tabelas automaticamente ao iniciar. A API fica disponível em `http://localhost:8080/api/v1`.

A conexão com o banco pode ser alterada pelas variáveis de ambiente `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.

## Testes

```bash
./mvnw verify
```

Esse comando roda todos os testes e gera o relatório de cobertura em `target/site/jacoco/index.html`.

| Tipo | Quantidade | O que testa |
|---|---|---|
| Unitários | 50 | As regras de negócio da camada de service, usando Mockito para isolar os repositories |
| Integração | 16 | O fluxo completo (HTTP → controller → service → banco) com MockMvc e um PostgreSQL real rodando no Docker via Testcontainers |

**Cobertura:** 100% das linhas e dos branches na camada de service.

Escolhi o Testcontainers em vez de um banco em memória (como o H2) para que os testes rodem no mesmo banco usado pela aplicação.

## Endpoints da API

Caminho base: `/api/v1`

| Método | Rota | Descrição |
|---|---|---|
| POST | `/users` | Cadastrar um cliente |
| GET | `/users/{id}` | Consultar um usuário |
| GET | `/categories` | Listar as categorias |
| POST | `/categories` | Criar uma categoria |
| PUT | `/categories/{id}` | Editar uma categoria |
| DELETE | `/categories/{id}` | Remover uma categoria |
| GET | `/menu-items?categoryId=&available=` | Listar os itens do cardápio (filtros opcionais) |
| GET | `/menu-items/{id}` | Consultar um item do cardápio |
| POST | `/menu-items` | Criar um item do cardápio |
| PUT | `/menu-items/{id}` | Editar um item do cardápio |
| PATCH | `/menu-items/{id}/availability` | Alterar a disponibilidade de um item |
| DELETE | `/menu-items/{id}` | Remover um item do cardápio |
| POST | `/orders` | Fazer um pedido |
| GET | `/orders?status=` | Listar os pedidos (filtro por status opcional) |
| GET | `/orders/{id}` | Consultar os detalhes de um pedido |
| PATCH | `/orders/{id}/cancel` | Cancelar um pedido |
| PATCH | `/orders/{id}/advance` | Avançar o pedido para o próximo status |

### Exemplo: fazendo um pedido

`POST /api/v1/orders`

```json
{
  "customerId": 1,
  "notes": "Sem cebola",
  "items": [
    { "menuItemId": 1, "quantity": 2 },
    { "menuItemId": 3, "quantity": 1 }
  ]
}
```

Resposta `201 Created`:

```json
{
  "id": 1,
  "customerId": 1,
  "status": "RECEIVED",
  "total": 71.00,
  "notes": "Sem cebola",
  "createdAt": "2026-10-19T16:57:42",
  "updatedAt": "2026-10-19T16:57:42",
  "items": [
    { "menuItemId": 1, "name": "Hambúrguer", "quantity": 2, "unitPrice": 32.00, "subtotal": 64.00 },
    { "menuItemId": 3, "name": "Refrigerante", "quantity": 1, "unitPrice": 7.00, "subtotal": 7.00 }
  ]
}
```

### Formato de erro

Todos os erros devolvem a mesma estrutura. A lista `fields` só aparece nos erros de validação.

```json
{
  "status": 400,
  "error": "Dados inválidos",
  "message": "Um ou mais campos estão inválidos",
  "path": "/api/v1/users",
  "timestamp": "2026-10-19T16:28:27",
  "fields": [
    { "field": "password", "message": "A senha deve ter no mínimo 8 caracteres" }
  ]
}
```

| Status | Quando acontece |
|---|---|
| 400 | Dados inválidos na requisição ou JSON malformado |
| 404 | Recurso não encontrado |
| 409 | Conflito com dados existentes (ex.: email já cadastrado) |
| 422 | Regra de negócio violada (ex.: cancelar um pedido que já está em preparo) |
| 500 | Erro inesperado (os detalhes ficam apenas no log, nunca na resposta) |

## Roadmap

- [x] Documentação (regras de negócio, casos de uso e contrato da API)
- [x] Funcionalidades principais (usuários, categorias, itens do cardápio e pedidos)
- [x] Tratamento global de erros, logs e migrations com Flyway
- [x] Testes unitários e de integração com relatório de cobertura
- [ ] Autenticação e autorização com Spring Security e JWT
- [ ] Desempenho: correção de N+1, paginação, índices e cache
- [ ] Documentação com Swagger / OpenAPI
- [ ] Docker Compose, CI com GitHub Actions e deploy na AWS

## Autor

**Daniel Rubio Camargo**, formado em Ciência da Computação pela Universidade Presbiteriana Mackenzie

[GitHub](https://github.com/dcamargoo)
