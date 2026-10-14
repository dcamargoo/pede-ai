package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.*;
import io.github.dcamargoo.pede_ai.entity.MenuItem;
import io.github.dcamargoo.pede_ai.entity.Order;
import io.github.dcamargoo.pede_ai.entity.OrderItem;
import io.github.dcamargoo.pede_ai.entity.User;
import io.github.dcamargoo.pede_ai.enums.OrderStatus;
import io.github.dcamargoo.pede_ai.exception.BusinessRuleException;
import io.github.dcamargoo.pede_ai.exception.ResourceNotFoundException;
import io.github.dcamargoo.pede_ai.repository.MenuItemRepository;
import io.github.dcamargoo.pede_ai.repository.OrderRepository;
import io.github.dcamargoo.pede_ai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        MenuItemRepository menuItemRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {

        // 1. O cliente precisa existir (404)
        User customer = userRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        // 2. O mesmo item não pode aparecer duas vezes na lista (422)
        validateNoDuplicateItems(request.items());

        // 3. Cria o pedido (status e createdAt são preenchidos pelo @PrePersist)
        Order order = new Order();
        order.setCustomer(customer);
        order.setNotes(request.notes());

        // 4. Monta cada item
        for (OrderItemRequestDTO itemRequest : request.items()) {

            MenuItem menuItem = menuItemRepository.findById(itemRequest.menuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Item do cardápio não encontrado: " + itemRequest.menuItemId()));

            // RN05: item indisponível não pode ser pedido (422)
            if (!menuItem.getAvailable()) {
                throw new BusinessRuleException("O item '" + menuItem.getName() + "' está indisponível");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.quantity());

            // RN07: o preço é COPIADO do cardápio no momento do pedido
            orderItem.setUnitPrice(menuItem.getPrice());

            // RN06: subtotal = preço × quantidade
            orderItem.setSubtotal(menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));

            // Liga o item ao pedido nos dois lados
            order.addItem(orderItem);
        }

        // 5. RN06: total = soma dos subtotais
        order.setTotal(calculateTotal(order));

        // 6. Salva o pedido; o cascade salva os itens junto
        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO findOrderById(Long id) {
        return toResponse(getOrderIfExists(id));
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponseDTO> listOrders(OrderStatus status) {
        List<Order> orders = (status != null)
                ? orderRepository.findByStatusOrderByCreatedAtDesc(status)
                : orderRepository.findAllByOrderByCreatedAtDesc();

        return orders.stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Long id) {
        Order order = getOrderIfExists(id);

        // RN09: só pode cancelar enquanto estiver RECEIVED (422)
        if (order.getStatus() != OrderStatus.RECEIVED) {
            throw new BusinessRuleException(
                    "Só é possível cancelar pedidos com status RECEIVED. Status atual: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    @Transactional
    public OrderResponseDTO advanceOrderStatus(Long id) {
        Order order = getOrderIfExists(id);

        // RN08: o status só avança para a próxima etapa
        order.setStatus(nextStatus(order.getStatus()));

        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    private Order getOrderIfExists(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
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