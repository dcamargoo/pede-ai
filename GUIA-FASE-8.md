# Guia da Fase 8 — Desempenho (Pede Aí)

> Arquivo de apoio. **Não commite este arquivo** (apague no final ou não dê `git add` nele).

Ordem sugerida, um commit por parte:

1. N+1 (obrigatório)
2. Índices (obrigatório, rápido)
3. Paginação (obrigatório)
4. Cache (opcional)
5. README

Depois de **cada** parte, rode:

```bash
./mvnw verify
```

Só faça o commit quando aparecer `BUILD SUCCESS`.

---

## Parte 1 — N+1

### O que é

Uma listagem faz **1 consulta** para buscar a lista e depois **mais 1 consulta para cada item** da lista, para buscar algo relacionado. Com 100 itens, são 101 consultas.

No Pede Aí, acontece em dois lugares:

| Rota | Causa |
|---|---|
| `GET /api/v1/menu-items` | O `toResponse` do `MenuItemService` chama `item.getCategory().getName()`. A categoria é `LAZY`, então cada item busca a sua categoria no banco. |
| `GET /api/v1/orders/{id}` | O `toItemResponse` do `OrderService` chama `item.getMenuItem().getName()`. Cada item do pedido busca o seu item do cardápio. |

O `GET /api/v1/orders` **não** tem N+1: o resumo usa só `getCustomer().getId()`, e o Hibernate já sabe o id sem ir ao banco.

### Como ver acontecendo (antes de corrigir)

1. Suba a aplicação.
2. Faça login como restaurante (`restaurante@pedeai.com` / `restaurante123`).
3. Crie 3 categorias e 1 item em cada uma.
4. Limpe o console do IntelliJ.
5. Chame `GET /api/v1/menu-items`.
6. Conte os `select` no console: deve aparecer **1 em `tb_menu_item`** e **3 em `tb_category`**.

### A correção: `@EntityGraph`

O `@EntityGraph` diz ao Spring Data: "nesta consulta, já traga junto o relacionamento X, usando `JOIN`". Assim tudo vem em **uma consulta só**.

#### 1.1 `MenuItemRepository`

Substitua o conteúdo da interface por:

```java
package io.github.dcamargoo.pede_ai.repository;

import io.github.dcamargoo.pede_ai.entity.MenuItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    boolean existsByCategoryId(Long categoryId);

    @Override
    @EntityGraph(attributePaths = "category")
    List<MenuItem> findAll();

    @EntityGraph(attributePaths = "category")
    List<MenuItem> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = "category")
    List<MenuItem> findByAvailable(Boolean available);

    @EntityGraph(attributePaths = "category")
    List<MenuItem> findByCategoryIdAndAvailable(Long categoryId, Boolean available);
}
```

- `attributePaths = "category"` é o **nome do campo** na entidade `MenuItem`, não o nome da tabela.
- O `findAll()` vem do `JpaRepository`. Para colocar a anotação nele, você "reescreve" a assinatura com `@Override`.

#### 1.2 `OrderRepository`

Adicione este método (mantenha os outros):

```java
@EntityGraph(attributePaths = {"items", "items.menuItem"})
Optional<Order> findWithItemsById(Long id);
```

Imports: `org.springframework.data.jpa.repository.EntityGraph` e `java.util.Optional`.

- `"items"` traz os itens do pedido, e `"items.menuItem"` traz o item do cardápio de cada um.
- O nome `findWithItemsById` funciona porque o Spring Data ignora o texto entre `find` e `By`. Para ele, é igual a `findById`, só que com um nome que explica o que faz.

#### 1.3 `OrderService`

No método privado `getOrderIfExists`, troque `orderRepository.findById(id)` por `orderRepository.findWithItemsById(id)`:

```java
private Order getOrderIfExists(Long id) {
    return orderRepository.findWithItemsById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
}
```

#### 1.4 `OrderServiceTest`

Os testes fazem `when(orderRepository.findById(...))`. Agora o service chama outro método, então eles quebram. Troque todos de uma vez:

```bash
sed -i 's/orderRepository.findById(/orderRepository.findWithItemsById(/g' src/test/java/io/github/dcamargoo/pede_ai/service/OrderServiceTest.java
```

Só esse arquivo. **Não** rode isso nos ITs: lá o `orderRepository.findById` é usado de propósito para conferir o banco.

#### 1.5 Conferir

1. `./mvnw verify` → `BUILD SUCCESS`.
2. Repita o teste do log: agora deve aparecer **1 select só**, com `join tb_category`.
3. Commit:

```bash
git add src
```

```bash
git commit -m "perf: corrigindo N+1 nas listagens de itens e nos detalhes do pedido com EntityGraph"
```

#### Frase para entrevista

> "Tinha N+1 na listagem do cardápio, porque a categoria é LAZY e o DTO precisa do nome dela. Vi no log do Hibernate e corrigi com `@EntityGraph`, que traz a categoria no mesmo SELECT com JOIN. Mantive o LAZY como padrão e só busco junto onde preciso."

---

## Parte 2 — Índices

### O que é

Um índice é como o índice de um livro: em vez de ler a tabela inteira, o banco vai direto nas linhas certas. O PostgreSQL **não** cria índice sozinho nas chaves estrangeiras (`customer_id`, `category_id`...), só nas chaves primárias e nos `UNIQUE`.

### 2.1 Crie a migration

Arquivo novo: `src/main/resources/db/migration/V3__create_indexes.sql`

```sql
CREATE INDEX idx_order_customer_id_created_at ON tb_order (customer_id, created_at);
CREATE INDEX idx_order_status_created_at ON tb_order (status, created_at);
CREATE INDEX idx_order_created_at ON tb_order (created_at);
CREATE INDEX idx_order_item_order_id ON tb_order_item (order_id);
CREATE INDEX idx_order_item_menu_item_id ON tb_order_item (menu_item_id);
CREATE INDEX idx_menu_item_category_id ON tb_menu_item (category_id);
```

| Índice | Ajuda em |
|---|---|
| `customer_id, created_at` | "Meus pedidos" (`/orders/me`), já ordenados por data |
| `status, created_at` | Restaurante filtrando por status (`/orders?status=RECEIVED`) |
| `created_at` | Restaurante listando todos, do mais novo ao mais antigo |
| `order_id` | Buscar os itens de um pedido |
| `menu_item_id` | Regra "item já pedido não pode ser removido" (`existsByMenuItemId`) |
| `category_id` | Filtrar itens por categoria e a regra "categoria com itens não pode ser removida" |

**Regra:** nunca edite a V1 ou a V2. Mudança no banco é sempre um arquivo novo com o próximo número.

### 2.2 Conferir

1. Suba a aplicação: o log deve mostrar `Migrating schema "public" to version "3 - create indexes"`.
2. No pgAdmin, rode:
   ```sql
   SELECT indexname FROM pg_indexes WHERE tablename = 'tb_order';
   ```
3. `./mvnw verify` → `BUILD SUCCESS`.
4. Commit:

```bash
git add src
```

```bash
git commit -m "perf: adicionando índices nas colunas mais consultadas"
```

---

## Parte 3 — Paginação

### O que é

Em vez de devolver todos os pedidos de uma vez, a API devolve uma "página" por vez:
`GET /api/v1/orders?page=0&size=20` → os 20 mais recentes.

Vamos paginar as duas listagens de pedidos: `GET /orders` (restaurante) e `GET /orders/me` (cliente). O cardápio fica sem paginação, porque é pequeno.

### 3.1 DTO novo: `dto/PageResponseDTO.java`

```java
package io.github.dcamargoo.pede_ai.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
```

**Por que um DTO próprio e não devolver o `Page` direto?** O JSON do `Page` do Spring tem muitos campos internos e pode mudar entre versões. O próprio Spring Data avisa para não devolver o `Page` direto na API. Com o DTO, o JSON fica sempre assim:

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 57,
  "totalPages": 3
}
```

O `<T>` é um **generic**: o mesmo DTO serve para qualquer tipo de lista.

### 3.2 `OrderRepository`

**Apague** estes três métodos:

```java
List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
List<Order> findAllByOrderByCreatedAtDesc();
List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
```

E **coloque** estes dois:

```java
Page<Order> findByStatus(OrderStatus status, Pageable pageable);
Page<Order> findByCustomerId(Long customerId, Pageable pageable);
```

Imports: `org.springframework.data.domain.Page` e `org.springframework.data.domain.Pageable`.

- A ordenação (`OrderByCreatedAtDesc`) saiu do nome do método porque agora ela vem dentro do `Pageable`.
- Não precisa criar o "listar todos": o `findAll(Pageable pageable)` já vem pronto do `JpaRepository`.
- Se o `import java.util.List` ficar sem uso no repository, apague.

### 3.3 `OrderService`

Substitua os métodos `listOrders` e `listCustomerOrders` por:

```java
@Transactional(readOnly = true)
public PageResponseDTO<OrderSummaryResponseDTO> listOrders(OrderStatus status, Pageable pageable) {
    Page<Order> orders;

    if (status != null) {
        orders = orderRepository.findByStatus(status, pageable);
    } else {
        orders = orderRepository.findAll(pageable);
    }

    return PageResponseDTO.from(orders.map(this::toSummary));
}

@Transactional(readOnly = true)
public PageResponseDTO<OrderSummaryResponseDTO> listCustomerOrders(User customer, Pageable pageable) {
    Page<Order> orders = orderRepository.findByCustomerId(customer.getId(), pageable);
    return PageResponseDTO.from(orders.map(this::toSummary));
}
```

Imports: `org.springframework.data.domain.Page` e `org.springframework.data.domain.Pageable`. O `PageResponseDTO` já entra pelo `import io.github.dcamargoo.pede_ai.dto.*;`.

O `orders.map(this::toSummary)` transforma cada `Order` da página em `OrderSummaryResponseDTO`, mantendo as informações de página (número, total...).

### 3.4 `OrderController`

Substitua os métodos `list` e `listMyOrders` por:

```java
@GetMapping
public ResponseEntity<PageResponseDTO<OrderSummaryResponseDTO>> list(
        @RequestParam(required = false) OrderStatus status,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    PageResponseDTO<OrderSummaryResponseDTO> response = orderService.listOrders(status, pageable);
    return ResponseEntity.ok(response);
}

@GetMapping("/me")
public ResponseEntity<PageResponseDTO<OrderSummaryResponseDTO>> listMyOrders(
        @AuthenticationPrincipal User customer,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    PageResponseDTO<OrderSummaryResponseDTO> response = orderService.listCustomerOrders(customer, pageable);
    return ResponseEntity.ok(response);
}
```

Imports:
- `io.github.dcamargoo.pede_ai.dto.PageResponseDTO`
- `org.springframework.data.domain.Pageable`
- `org.springframework.data.domain.Sort`
- `org.springframework.data.web.PageableDefault`

O Spring monta o `Pageable` sozinho a partir da URL: `?page=1&size=10&sort=total,asc`. Se a URL não tiver nada, vale o `@PageableDefault`: página 0, 20 por página, mais novos primeiro.

### 3.5 Limite de tamanho no `application.yml`

Sem limite, alguém pode pedir `?size=1000000`. Coloque **dentro do bloco `spring:` que já existe** (mesmo nível do `jpa:`, com 2 espaços), não crie outro `spring:`:

```yaml
  data:
    web:
      pageable:
        max-page-size: 100
```

### 3.6 Erro 400 para ordenação inválida

Se alguém mandar `?sort=campoQueNaoExiste`, o Spring lança `PropertyReferenceException` e hoje isso viraria 500. No `GlobalExceptionHandler`, adicione:

```java
@ExceptionHandler(PropertyReferenceException.class)
public ResponseEntity<ErrorResponseDTO> handleInvalidSort(PropertyReferenceException ex,
                                                          HttpServletRequest request) {
    log.warn("Ordenação inválida em {}: {}", request.getRequestURI(), ex.getMessage());

    return buildResponse(
            HttpStatus.BAD_REQUEST,
            "Dados inválidos",
            "Campo de ordenação inválido: " + ex.getPropertyName(),
            request,
            null);
}
```

Import: `org.springframework.data.mapping.PropertyReferenceException`.

### 3.7 `OrderServiceTest`

Substitua o `@Nested class ListOrders` e o `@Nested class ListCustomerOrders` inteiros por:

```java
@Nested
class ListOrders {

    @Test
    void shouldListOrdersByOrderStatus_whenOrderStatusIsPresent() {
        Order order = buildOrder(OrderStatus.RECEIVED);
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findByStatus(OrderStatus.RECEIVED, pageable))
                .thenReturn(new PageImpl<>(List.of(order), pageable, 1));

        PageResponseDTO<OrderSummaryResponseDTO> response = orderService.listOrders(OrderStatus.RECEIVED, pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).status()).isEqualTo(OrderStatus.RECEIVED);
        assertThat(response.totalElements()).isEqualTo(1);
        verify(orderRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void shouldListAllOrders_whenOrderStatusIsNotPresent() {
        Order receivedOrder = buildOrder(OrderStatus.RECEIVED);
        Order deliveredOrder = buildOrder(OrderStatus.DELIVERED);
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(receivedOrder, deliveredOrder), pageable, 2));

        PageResponseDTO<OrderSummaryResponseDTO> response = orderService.listOrders(null, pageable);

        assertThat(response.content()).hasSize(2);
        assertThat(response.totalElements()).isEqualTo(2);
        verify(orderRepository, never()).findByStatus(any(), any());
    }
}

@Nested
class ListCustomerOrders {

    @Test
    void shouldListOnlyOrdersOfLoggedCustomer() {
        Order order = buildOrder(OrderStatus.RECEIVED);
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findByCustomerId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(order), pageable, 1));

        PageResponseDTO<OrderSummaryResponseDTO> response = orderService.listCustomerOrders(customer, pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).customerId()).isEqualTo(1L);
        verify(orderRepository, never()).findAll(any(Pageable.class));
    }
}
```

Imports novos no teste:
- `io.github.dcamargoo.pede_ai.dto.PageResponseDTO`
- `org.springframework.data.domain.PageImpl`
- `org.springframework.data.domain.PageRequest`
- `org.springframework.data.domain.Pageable`

O `PageImpl` é uma página "de mentira" para o mock devolver: `new PageImpl<>(lista, pageable, totalDeElementos)`.

### 3.8 `OrderControllerIT`

No teste `shouldListOnlyOrdersOfLoggedCustomer_whenCallingMyOrders`, a lista agora fica dentro de `content`. Troque:

```java
.andExpect(jsonPath("$.length()").value(1))
.andExpect(jsonPath("$[0].customerId").value(marina.getId()));
```

por:

```java
.andExpect(jsonPath("$.content.length()").value(1))
.andExpect(jsonPath("$.content[0].customerId").value(marina.getId()))
.andExpect(jsonPath("$.totalElements").value(1));
```

E adicione este teste novo na classe:

```java
@Test
void shouldReturnRequestedPage_whenRestaurantListsOrders() throws Exception {
    User restaurant = createRestaurant("restaurante@email.com");
    User customer = createCustomer("marina@email.com");
    Category category = createCategory("Lanches");
    MenuItem burger = createMenuItem("Hambúrguer", "32.00", true, category);
    createOrder(customer, burger, 1);
    createOrder(customer, burger, 2);
    createOrder(customer, burger, 3);

    mockMvc.perform(get("/api/v1/orders")
                    .param("page", "0")
                    .param("size", "2")
                    .header("Authorization", bearerToken(restaurant)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(2));
}
```

### 3.9 Conferir e commitar

1. `./mvnw verify` → `BUILD SUCCESS`.
2. No Postman, como restaurante: `GET /api/v1/orders?page=0&size=2`.
3. Commit:

```bash
git add src
```

```bash
git commit -m "feat: adicionando paginação nas listagens de pedidos"
```

---

## Parte 4 — Cache (opcional)

### O que é

O cardápio é muito consultado e muda pouco. Com cache, a primeira consulta vai ao banco e guarda o resultado na memória. As próximas devolvem direto da memória, até alguém alterar o cardápio.

**O ponto mais importante:** sempre que o cardápio mudar, o cache precisa ser **limpo**. Senão, a API devolve dados velhos.

### 4.1 Dependência no `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

### 4.2 Ligar o cache: `config/CacheConfig.java`

```java
package io.github.dcamargoo.pede_ai.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
}
```

### 4.3 `MenuItemService`

- No `listItems`: `@Cacheable("menuItems")`
- No `createItem`, `updateItem`, `updateAvailability` e `deleteItem`: `@CacheEvict(value = "menuItems", allEntries = true)`

Exemplo:

```java
@Cacheable("menuItems")
@Transactional(readOnly = true)
public List<MenuItemResponseDTO> listItems(Long categoryId, Boolean available) {
```

```java
@CacheEvict(value = "menuItems", allEntries = true)
@Transactional
public MenuItemResponseDTO createItem(MenuItemRequestDTO request){
```

Imports: `org.springframework.cache.annotation.Cacheable` e `org.springframework.cache.annotation.CacheEvict`.

- `@Cacheable` guarda o resultado, separado por combinação de parâmetros (`categoryId` e `available`).
- `@CacheEvict(allEntries = true)` apaga **todas** as listas guardadas, porque qualquer mudança num item pode afetar qualquer filtro.

### 4.4 `CategoryService`

- No `listCategories`: `@Cacheable("categories")`
- No `createCategory`: `@CacheEvict(value = "categories", allEntries = true)`
- No `updateCategory` e no `deleteCategory`: `@CacheEvict(value = {"categories", "menuItems"}, allEntries = true)`

Por que o `updateCategory` limpa também o `menuItems`? Porque a resposta de cada item mostra o **nome da categoria**. Se a categoria mudar de nome e o cache de itens não for limpo, o cardápio mostra o nome antigo.

### 4.5 Limpar o cache entre os testes de integração

Os ITs compartilham a mesma aplicação, e o `@BeforeEach` apaga o banco, mas não o cache. No `AbstractIntegrationTest`:

```java
@Autowired
private CacheManager cacheManager;
```

E no final do método `cleanDatabase()`:

```java
cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
```

Import: `org.springframework.cache.CacheManager`.

### 4.6 Conferir e commitar

1. `./mvnw verify` → `BUILD SUCCESS`.
2. Teste no log: chame `GET /api/v1/menu-items` duas vezes. Na segunda, **nenhum select** deve aparecer. Depois crie um item e chame de novo: o select volta a aparecer uma vez.
3. Commit:

```bash
git add pom.xml src
```

```bash
git commit -m "perf: adicionando cache no cardápio e nas categorias"
```

#### Limitação para comentar em entrevista

O cache fica na memória de cada instância da aplicação. Com 2 servidores, cada um teria o seu cache, e um poderia ficar desatualizado. Em produção, o comum é usar o **Redis** como cache compartilhado.

---

## Parte 5 — README

1. **Endpoints:** nas linhas do `GET /orders?status=` e do `GET /orders/me`, acrescente na descrição: `(paginado: page, size, sort)`.
2. **Algumas decisões:** acrescente:
   ```markdown
   - **Sem N+1:** as consultas que precisam de relacionamentos usam `@EntityGraph`, trazendo tudo em um único SELECT com JOIN.
   - **Paginação:** as listagens de pedidos são paginadas (20 por página, no máximo 100), e a resposta usa um DTO próprio.
   - **Índices:** a migration V3 cria índices nas colunas usadas em filtros, ordenações e chaves estrangeiras.
   - **Cache:** o cardápio e as categorias ficam em cache e são limpos sempre que algo muda.
   ```
   (Tire a linha do cache se não fizer a Parte 4.)
3. **Testes:** atualize as quantidades da tabela com os números do último `./mvnw verify` (linha `Tests run:` dos unitários e dos de integração; nos unitários, subtraia 1, que é o `PedeAiApplicationTests`).
4. **Roadmap:** marque `- [x] Desempenho: correção de N+1, paginação, índices e cache`.

Commit:

```bash
git add README.md
```

```bash
git commit -m "docs: atualizando o README com a fase de desempenho"
```

```bash
git push
```

---

## Se der erro

| Erro | Causa provável |
|---|---|
| `No property 'xxx' found for type 'Order'` ao subir | Nome de método do repository errado (ex.: `findByCustomer_Id` em vez de `findByCustomerId`). |
| `MultipleBagFetchException` | Dois `@EntityGraph` de listas ao mesmo tempo. Neste guia só há uma lista (`items`), então não deve acontecer. |
| Teste unitário com `PotentialStubbingProblem` | O `when(...)` está com argumento diferente do que o service chama (ex.: outro `Pageable`). Use o mesmo objeto `pageable` no `when` e na chamada. |
| Flyway: `Validate failed: Migration checksum mismatch` | Você editou uma migration que já tinha rodado. Volte o arquivo ao original e crie uma migration nova. |
| `PropertyReferenceException` virando 500 | Faltou o handler da seção 3.6. |
| IT do cache falhando com dado de outro teste | Faltou limpar o cache no `cleanDatabase()` (seção 4.5). |

---

## Prompt para usar no ChatGPT

Copie e cole isto no começo da conversa, junto com o código do arquivo em que você travou e a mensagem de erro completa:

```
Estou desenvolvendo o Pede Aí, uma API REST de pedidos de delivery de um único restaurante, como projeto de portfólio para vagas de Desenvolvedor Backend Java Júnior.

Stack: Java 21, Spring Boot 4.1.1 (Spring Framework 7, Hibernate 7, Jackson 3, Spring Security 7), PostgreSQL 16, Flyway, Maven Wrapper, Lombok. Testes: JUnit 5, Mockito, AssertJ, MockMvc e Testcontainers. Testes unitários terminam em Test (Surefire) e de integração em IT (Failsafe). Rodo tudo com ./mvnw verify.

Arquitetura em camadas: Controller → Service → Repository. DTOs são records. Erros tratados num @RestControllerAdvice (GlobalExceptionHandler) com o formato ErrorResponseDTO. Mensagens de erro da API em português; código e nomes em inglês. Testes no padrão shouldX_whenY, agrupados com @Nested.

Segurança: JWT (jjwt) com filtro próprio, papéis CUSTOMER e RESTAURANT, cliente logado obtido com @AuthenticationPrincipal User. Nos ITs, uso o helper bearerToken(user) da classe AbstractIntegrationTest para mandar o header Authorization.

Estou na Fase 8 (desempenho): corrigir N+1 com @EntityGraph, criar índices numa migration V3, paginar as listagens de pedidos com Pageable e um PageResponseDTO próprio, e (opcional) cache com Spring Cache.

Regras para a sua resposta:
- Sou júnior: explique de forma simples, passo a passo.
- Não coloque comentários no código.
- Mantenha o mesmo estilo do código que eu enviar.
- Se precisar de algum arquivo para responder, peça antes de supor.
```
