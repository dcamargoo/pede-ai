package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.dto.OrderRequestDTO;
import io.github.dcamargoo.pede_ai.dto.OrderResponseDTO;
import io.github.dcamargoo.pede_ai.dto.OrderSummaryResponseDTO;
import io.github.dcamargoo.pede_ai.dto.PageResponseDTO;
import io.github.dcamargoo.pede_ai.entity.User;
import io.github.dcamargoo.pede_ai.enums.OrderStatus;
import io.github.dcamargoo.pede_ai.service.OrderService;
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
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderRequestDTO request,
                                                   @AuthenticationPrincipal User customer) {
        OrderResponseDTO response = orderService.createOrder(request, customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable Long id,
                                                     @AuthenticationPrincipal User user) {
        OrderResponseDTO response = orderService.findOrderById(id, user);
        return ResponseEntity.ok(response);
    }

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

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancel(@PathVariable Long id,
                                                   @AuthenticationPrincipal User customer) {
        OrderResponseDTO response = orderService.cancelOrder(id, customer);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/advance")
    public ResponseEntity<OrderResponseDTO> advance(@PathVariable Long id) {
        OrderResponseDTO response = orderService.advanceOrderStatus(id);
        return ResponseEntity.ok(response);
    }
}
