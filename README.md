# Pede Aí

API REST para gerenciar pedidos de delivery de um único restaurante, desenvolvida com **Java 21** e **Spring Boot 4**.

> 🚧 **Em desenvolvimento.** O próximo passo é o deploy. Veja o [Roadmap](#roadmap).

## Sobre o projeto

Restaurantes pequenos costumam receber pedidos por mensagem. O cardápio fica desatualizado, o total é calculado na mão e ninguém sabe em que etapa cada pedido está.

O **Pede Aí** resolve isso centralizando o cardápio e os pedidos em uma API:

- O **cliente** consulta o cardápio, faz pedidos e acompanha ou cancela cada um deles.
- O **restaurante** gerencia as categorias e os itens do cardápio, e avança o status de cada pedido até a entrega.

**Exemplo:** Marina pede 2 hambúrgueres (R$ 32,00 cada) e 1 refrigerante (R$ 7,00). A API calcula o total de **R$ 71,00** e cria o pedido com o status `RECEIVED`. Depois, o restaurante muda o status para `IN_PREPARATION`, `OUT_FOR_DELIVERY` e, por fim, `DELIVERED`.

## Funcionalidades

- Cadastro de clientes e login com JWT
- Permissões por papel: cliente (`CUSTOMER`) e restaurante (`RESTAURANT`)
- Categorias do cardápio (criar, editar, remover e listar)
- Itens do cardápio (criar, editar, remover, filtrar por categoria e disponibilidade, marcar como indisponível)
- Pedidos (criar, listar com filtro por status, consultar detalhes, cancelar e avançar o status)
- Limite de tentativas de login (5 por minuto por IP)
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
| Pedido do próprio cliente | O cliente do pedido vem do token, nunca do JSON. Cada cliente só vê e cancela os próprios pedidos. |

## Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4 (Web MVC, Data JPA, Validation, Security) |
| Segurança | Spring Security, JWT (jjwt), BCrypt e Bucket4j |
| Banco de dados | PostgreSQL 16 |
| Migrations | Flyway |
| Build | Maven |
| Testes | JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers |
| Documentação | springdoc-openapi (Swagger UI) |
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
├── config       configuração do Spring Security e do BCrypt
├── controller   endpoints REST
├── service      regras de negócio
├── repository   repositories do Spring Data JPA
├── entity       entidades JPA
├── dto          records de requisição e de resposta
├── enums        Role, OrderStatus
├── exception    exceções personalizadas e tratamento global de erros
└── security     filtro JWT, geração do token e limite de tentativas de login
```

### Algumas decisões

- **DTOs como records do Java:** as entidades nunca são devolvidas pela API. Assim, dados sensíveis, como a senha do usuário, nunca saem da aplicação.
- **Tratamento global de erros:** um único `@RestControllerAdvice` transforma cada exceção no mesmo formato de erro, com o status HTTP correto.
- **Flyway + `ddl-auto: validate`:** o Flyway cria e altera o banco por meio de scripts SQL versionados. O Hibernate apenas valida se as entidades batem com as tabelas.
- **Relacionamentos `LAZY`:** os dados relacionados só são carregados quando são necessários.
- **O `PATCH /orders/{id}/advance` não recebe o novo status:** a API move o pedido para a próxima etapa, então é impossível pular etapas.
- **Senhas com BCrypt:** o banco guarda apenas o hash da senha, nunca a senha original.
- **Autenticação stateless com JWT:** o servidor não guarda sessão. Cada requisição envia o token no header `Authorization: Bearer <token>`.
- **Pedido de outro cliente devolve 404, e não 403:** assim a API não revela que o pedido existe.
- **Limite de tentativas de login:** cada IP pode tentar o login 5 vezes por minuto. Depois disso, a API devolve 429, o que dificulta ataques de força bruta.
- **N+1 resolvido com `@EntityGraph`:** como os relacionamentos são `LAZY`, listar o cardápio fazia 1 consulta para os itens e mais 1 para cada categoria, e buscar um pedido fazia uma consulta para cada item. Com o `@EntityGraph`, o Hibernate traz tudo junto em 1 consulta só (verificado no log do SQL).
- **Paginação nas listas de pedidos:** `GET /orders` e `GET /orders/me` devolvem páginas de 20 itens por padrão, do pedido mais novo para o mais antigo. O tamanho máximo é 100 (um valor maior é reduzido para 100) e um campo de ordenação inexistente devolve 400. A resposta usa um `PageResponseDTO` próprio, para o formato da API não depender do `Page` do Spring.

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

A conexão com o banco pode ser alterada pelas variáveis de ambiente `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`. A chave que assina os tokens vem da variável `JWT_SECRET`.

### 4. Usuário do restaurante

O Flyway cria um usuário do restaurante para testes locais:

| Email | Senha |
|---|---|
| `restaurante@pedeai.com` | `restaurante123` |

Os clientes são criados pelo `POST /api/v1/users`.

> ⚠️ A chave padrão do JWT e a senha acima servem **apenas para desenvolvimento**. Em produção, defina a variável `JWT_SECRET` e troque a senha do restaurante.

## Testes

```bash
./mvnw verify
```

Esse comando roda todos os testes e gera o relatório de cobertura em `target/site/jacoco/index.html`.

| Tipo | Quantidade | O que testa |
|---|---|---|
| Unitários | 57 | As regras de negócio da camada de service, usando Mockito para isolar os repositories |
| Integração | 37 | O fluxo completo (HTTP → segurança → controller → service → banco) com MockMvc e um PostgreSQL real rodando no Docker via Testcontainers |

**Cobertura:** 93% das linhas e 97% dos branches no projeto todo, e 100% das linhas e dos branches na camada de service.

Escolhi o Testcontainers em vez de um banco em memória (como o H2) para que os testes rodem no mesmo banco usado pela aplicação.

## Documentação com Swagger

Com a aplicação rodando, a documentação interativa da API fica em:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Especificação OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

Nela dá para ver todos os endpoints, os campos de cada requisição, as respostas possíveis (incluindo os erros) e testar tudo pelo navegador, sem precisar do Postman.

### Testando rotas protegidas

1. Abra o `POST /auth/login`, clique em **Try it out** e faça login (por exemplo, com o usuário do restaurante).
2. Copie o valor de `token` da resposta.
3. Clique no botão **Authorize**, no topo da página, e cole **só o token**, sem escrever `Bearer`.
4. Pronto: as próximas requisições já vão com o token.

> ⚠️ Em produção, considere desligar o Swagger com `springdoc.swagger-ui.enabled=false` e `springdoc.api-docs.enabled=false`, para não expor a documentação da API publicamente.

## Endpoints da API

Caminho base: `/api/v1`

As rotas protegidas precisam do header `Authorization: Bearer <token>`. O token é obtido no `POST /auth/login`.

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/users` | Cadastrar um cliente | Público |
| POST | `/auth/login` | Fazer login e receber o token | Público |
| GET | `/users/me` | Consultar o usuário logado | Logado |
| GET | `/users/{id}` | Consultar um usuário | Restaurante |
| GET | `/categories` | Listar as categorias | Público |
| POST | `/categories` | Criar uma categoria | Restaurante |
| PUT | `/categories/{id}` | Editar uma categoria | Restaurante |
| DELETE | `/categories/{id}` | Remover uma categoria | Restaurante |
| GET | `/menu-items?categoryId=&available=` | Listar os itens do cardápio (filtros opcionais) | Público |
| GET | `/menu-items/{id}` | Consultar um item do cardápio | Público |
| POST | `/menu-items` | Criar um item do cardápio | Restaurante |
| PUT | `/menu-items/{id}` | Editar um item do cardápio | Restaurante |
| PATCH | `/menu-items/{id}/availability` | Alterar a disponibilidade de um item | Restaurante |
| DELETE | `/menu-items/{id}` | Remover um item do cardápio | Restaurante |
| POST | `/orders` | Fazer um pedido | Cliente |
| GET | `/orders/me?page=&size=` | Listar os pedidos do cliente logado (paginado) | Cliente |
| GET | `/orders?status=&page=&size=` | Listar todos os pedidos (filtro por status opcional, paginado) | Restaurante |
| GET | `/orders/{id}` | Consultar os detalhes de um pedido | Logado (o cliente só vê os próprios) |
| PATCH | `/orders/{id}/cancel` | Cancelar um pedido | Cliente (só os próprios) |
| PATCH | `/orders/{id}/advance` | Avançar o pedido para o próximo status | Restaurante |

### Exemplo: fazendo login

`POST /api/v1/auth/login`

```json
{
  "email": "marina@email.com",
  "password": "senha123"
}
```

Resposta `200 OK`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

O token vale por 2 horas.

### Exemplo: fazendo um pedido

`POST /api/v1/orders`, com o header `Authorization: Bearer <token>` de um cliente:

```json
{
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
| 401 | Login inválido, ou token ausente, inválido ou expirado |
| 403 | O usuário está logado, mas o papel dele não tem permissão para a rota |
| 404 | Recurso não encontrado |
| 409 | Conflito com dados existentes (ex.: email já cadastrado) |
| 422 | Regra de negócio violada (ex.: cancelar um pedido que já está em preparo) |
| 429 | Mais de 5 tentativas de login por minuto vindas do mesmo IP |
| 500 | Erro inesperado (os detalhes ficam apenas no log, nunca na resposta) |

## Roadmap

- [x] Documentação (regras de negócio, casos de uso e contrato da API)
- [x] Funcionalidades principais (usuários, categorias, itens do cardápio e pedidos)
- [x] Tratamento global de erros, logs e migrations com Flyway
- [x] Testes unitários e de integração com relatório de cobertura
- [x] Autenticação e autorização com Spring Security e JWT
- [x] Limite de tentativas de login (rate limiting)
- [x] Desempenho: correção de N+1 e paginação
- [x] Documentação com Swagger / OpenAPI
- [ ] Docker Compose, CI com GitHub Actions e deploy na AWS

## Autor

**Daniel Rubio Camargo**, formado em Ciência da Computação pela Universidade Presbiteriana Mackenzie
