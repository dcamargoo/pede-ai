package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.dto.OrderRequestDTO;
import io.github.dcamargoo.pede_ai.dto.OrderResponseDTO;
import io.github.dcamargoo.pede_ai.dto.OrderSummaryResponseDTO;
import io.github.dcamargoo.pede_ai.dto.PageResponseDTO;
import io.github.dcamargoo.pede_ai.entity.User;
import io.github.dcamargoo.pede_ai.enums.OrderStatus;
import io.github.dcamargoo.pede_ai.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Pedidos", description = "Criação, consulta e acompanhamento dos pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Cria um pedido")
    @ApiResponse(responseCode = "201", description = "Pedido criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas clientes podem criar pedidos")
    @ApiResponse(responseCode = "404", description = "Item do cardápio não encontrado")
    @ApiResponse(responseCode = "422", description = "Item indisponível ou repetido no pedido")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderRequestDTO request,
                                                   @AuthenticationPrincipal User customer) {
        OrderResponseDTO response = orderService.createOrder(request, customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Busca um pedido pelo id", description = "O cliente só enxerga os próprios pedidos. O restaurante enxerga todos")
    @ApiResponse(responseCode = "200", description = "Pedido encontrado")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable Long id,
                                                     @AuthenticationPrincipal User user) {
        OrderResponseDTO response = orderService.findOrderById(id, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lista todos os pedidos (paginado)", description = "Aceita filtro por status")
    @ApiResponse(responseCode = "200", description = "Página de pedidos")
    @ApiResponse(responseCode = "400", description = "Status inválido")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas o restaurante pode listar todos os pedidos")
    @GetMapping
    public ResponseEntity<PageResponseDTO<OrderSummaryResponseDTO>> list(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDTO<OrderSummaryResponseDTO> response = orderService.listOrders(status, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lista os pedidos do cliente logado (paginado)")
    @ApiResponse(responseCode = "200", description = "Página de pedidos do cliente")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas clientes possuem pedidos próprios")
    @GetMapping("/me")
    public ResponseEntity<PageResponseDTO<OrderSummaryResponseDTO>> listMyOrders(
            @AuthenticationPrincipal User customer,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDTO<OrderSummaryResponseDTO> response = orderService.listCustomerOrders(customer, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancela um pedido", description = "Só é possível cancelar pedidos com status RECEIVED")
    @ApiResponse(responseCode = "200", description = "Pedido cancelado")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas clientes podem cancelar pedidos")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @ApiResponse(responseCode = "422", description = "O pedido não está mais com status RECEIVED")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancel(@PathVariable Long id,
                                                   @AuthenticationPrincipal User customer) {
        OrderResponseDTO response = orderService.cancelOrder(id, customer);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Avança o pedido para o próximo status", description = "RECEIVED, IN_PREPARATION, OUT_FOR_DELIVERY e DELIVERED, nessa ordem")
    @ApiResponse(responseCode = "200", description = "Status avançado")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas o restaurante pode avançar o status")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @ApiResponse(responseCode = "422", description = "O pedido já está finalizado")
    @PatchMapping("/{id}/advance")
    public ResponseEntity<OrderResponseDTO> advance(@PathVariable Long id) {
        OrderResponseDTO response = orderService.advanceOrderStatus(id);
        return ResponseEntity.ok(response);
    }
}
