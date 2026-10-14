package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.dto.OrderRequestDTO;
import io.github.dcamargoo.pede_ai.dto.OrderResponseDTO;
import io.github.dcamargoo.pede_ai.dto.OrderSummaryResponseDTO;
import io.github.dcamargoo.pede_ai.enums.OrderStatus;
import io.github.dcamargoo.pede_ai.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderRequestDTO request) {
        OrderResponseDTO response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable Long id) {
        OrderResponseDTO response = orderService.findOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryResponseDTO>> list(
            @RequestParam(required = false) OrderStatus status) {
        List<OrderSummaryResponseDTO> response = orderService.listOrders(status);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancel(@PathVariable Long id) {
        OrderResponseDTO response = orderService.cancelOrder(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/advance")
    public ResponseEntity<OrderResponseDTO> advance(@PathVariable Long id) {
        OrderResponseDTO response = orderService.advanceOrderStatus(id);
        return ResponseEntity.ok(response);
    }
}
