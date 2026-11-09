package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.*;
import io.github.dcamargoo.pede_ai.entity.MenuItem;
import io.github.dcamargoo.pede_ai.entity.Order;
import io.github.dcamargoo.pede_ai.entity.OrderItem;
import io.github.dcamargoo.pede_ai.entity.User;
import io.github.dcamargoo.pede_ai.enums.OrderStatus;
import io.github.dcamargoo.pede_ai.enums.Role;
import io.github.dcamargoo.pede_ai.exception.BusinessRuleException;
import io.github.dcamargoo.pede_ai.exception.ResourceNotFoundException;
import io.github.dcamargoo.pede_ai.repository.MenuItemRepository;
import io.github.dcamargoo.pede_ai.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.github.dcamargoo.pede_ai.dto.PageResponseDTO;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;

    public OrderService(OrderRepository orderRepository,
                        MenuItemRepository menuItemRepository) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request, User customer) {

        validateNoDuplicateItems(request.items());

        Order order = new Order();
        order.setCustomer(customer);
        order.setNotes(request.notes());

        for (OrderItemRequestDTO itemRequest : request.items()) {

            MenuItem menuItem = menuItemRepository.findById(itemRequest.menuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Item do cardápio não encontrado: " + itemRequest.menuItemId()));

            if (!menuItem.getAvailable()) {
                throw new BusinessRuleException("O item '" + menuItem.getName() + "' está indisponível");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(menuItem.getPrice());
            orderItem.setSubtotal(menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));

            order.addItem(orderItem);
        }

        order.setTotal(calculateTotal(order));

        Order savedOrder = orderRepository.save(order);
        log.info("Pedido {} criado pelo cliente {} com total R$ {}", savedOrder.getId(), customer.getId(), savedOrder.getTotal());
        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO findOrderById(Long id, User user) {
        return toResponse(getOrderVisibleTo(id, user));
    }

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

    @Transactional
    public OrderResponseDTO cancelOrder(Long id, User customer) {
        Order order = getOrderVisibleTo(id, customer);

        if (order.getStatus() != OrderStatus.RECEIVED) {
            throw new BusinessRuleException(
                    "Só é possível cancelar pedidos com status RECEIVED. Status atual: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.saveAndFlush(order);
        log.info("Pedido {} cancelado", savedOrder.getId());
        return toResponse(savedOrder);
    }

    @Transactional
    public OrderResponseDTO advanceOrderStatus(Long id) {
        Order order = getOrderIfExists(id);

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(nextStatus(previousStatus));

        Order savedOrder = orderRepository.saveAndFlush(order);
        log.info("Pedido {} avançou de {} para {}", savedOrder.getId(), previousStatus, savedOrder.getStatus());
        return toResponse(savedOrder);
    }

    private Order getOrderIfExists(Long id) {
        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
    }

    private Order getOrderVisibleTo(Long id, User user) {
        Order order = getOrderIfExists(id);

        boolean isCustomer = user.getRole() == Role.CUSTOMER;
        boolean isOwner = order.getCustomer().getId().equals(user.getId());
        if (isCustomer && !isOwner) {
            throw new ResourceNotFoundException("Pedido não encontrado");
        }

        return order;
    }

    private void validateNoDuplicateItems(List<OrderItemRequestDTO> items) {
        Set<Long> seen = new HashSet<>();
        for (OrderItemRequestDTO item : items) {
            if (!seen.add(item.menuItemId())) {
                throw new BusinessRuleException(
                        "O item " + item.menuItemId() + " aparece mais de uma vez. Use a quantidade.");
            }
        }
    }

    private BigDecimal calculateTotal(Order order) {
        return order.getItems().stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderStatus nextStatus(OrderStatus current) {
        return switch (current) {
            case RECEIVED -> OrderStatus.IN_PREPARATION;
            case IN_PREPARATION -> OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> throw new BusinessRuleException(
                    "O pedido já está finalizado com status " + current);
        };
    }

    private OrderResponseDTO toResponse(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getCustomer().getId(),
                order.getStatus(),
                order.getTotal(),
                order.getNotes(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream()
                        .map(this::toItemResponse)
                        .toList()
        );
    }

    private OrderItemResponseDTO toItemResponse(OrderItem item) {
        return new OrderItemResponseDTO(
                item.getMenuItem().getId(),
                item.getMenuItem().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    private OrderSummaryResponseDTO toSummary(Order order) {
        return new OrderSummaryResponseDTO(
                order.getId(),
                order.getCustomer().getId(),
                order.getStatus(),
                order.getTotal(),
                order.getCreatedAt()
        );
    }
}