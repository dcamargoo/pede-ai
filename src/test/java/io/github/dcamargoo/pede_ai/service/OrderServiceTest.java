package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.OrderItemRequestDTO;
import io.github.dcamargoo.pede_ai.dto.OrderRequestDTO;
import io.github.dcamargoo.pede_ai.dto.OrderResponseDTO;
import io.github.dcamargoo.pede_ai.dto.OrderSummaryResponseDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private OrderService orderService;

    private User customer;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(1L);
        customer.setName("Marina");
    }

    private Order buildOrder(OrderStatus status) {
        Order order = new Order();
        order.setId(10L);
        order.setCustomer(customer);
        order.setStatus(status);
        order.setTotal(new BigDecimal("50.00"));
        return order;
    }

    private MenuItem buildMenuItem(Long id, String name, String price, boolean available) {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(id);
        menuItem.setName(name);
        menuItem.setPrice(new BigDecimal(price));
        menuItem.setAvailable(available);
        return menuItem;
    }

    @Nested
    class CancelOrder {

        @Test
        void shouldCancelOrder_whenStatusIsReceived() {
            Order order = buildOrder(OrderStatus.RECEIVED);
            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
            when(orderRepository.saveAndFlush(order)).thenReturn(order);

            OrderResponseDTO response = orderService.cancelOrder(10L);

            assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
            verify(orderRepository).saveAndFlush(order);
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"IN_PREPARATION", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"})
        void shouldThrowBusinessRuleException_whenStatusIsNotReceived(OrderStatus status) {
            Order order = buildOrder(status);
            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                    () -> orderService.cancelOrder(10L));

            assertThat(exception.getMessage()).contains("RECEIVED");
            verify(orderRepository, never()).saveAndFlush(any());
        }

        @Test
        void shouldThrowResourceNotFoundException_whenOrderDoesNotExist() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> orderService.cancelOrder(99L));

            verify(orderRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class AdvanceOrderStatus {

        @ParameterizedTest
        @CsvSource({"RECEIVED, IN_PREPARATION",
                "IN_PREPARATION, OUT_FOR_DELIVERY",
                "OUT_FOR_DELIVERY, DELIVERED"})
        void shouldAdvanceOrderStatus_whenOrderStatusIsNotFinished(OrderStatus currentStatus, OrderStatus expectedStatus) {
            Order order = buildOrder(currentStatus);
            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
            when(orderRepository.saveAndFlush(order)).thenReturn(order);

            OrderResponseDTO response = orderService.advanceOrderStatus(10L);

            assertThat(response.status()).isEqualTo(expectedStatus);
            verify(orderRepository).saveAndFlush(order);
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"DELIVERED", "CANCELLED"})
        void shouldThrowBusinessRuleException_whenOrderIsAlreadyFinished(OrderStatus currentStatus) {
            Order order = buildOrder(currentStatus);
            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                    () -> orderService.advanceOrderStatus(10L));

            assertThat(exception.getMessage()).contains(currentStatus.name());
            verify(orderRepository, never()).saveAndFlush(any());
        }

        @Test
        void shouldThrowResourceNotFoundException_whenOrderDoesNotExist() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> orderService.advanceOrderStatus(99L));

            verify(orderRepository, never()).saveAndFlush(any());
        }

    }

    @Nested
    class ListOrders {

        @Test
        void shouldListOrdersByOrderStatus_whenOrderStatusIsPresent() {
            Order order = buildOrder(OrderStatus.RECEIVED);
            when(orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.RECEIVED)).thenReturn(List.of(order));

            List<OrderSummaryResponseDTO> response = orderService.listOrders(OrderStatus.RECEIVED);

            assertThat(response).hasSize(1);
            assertThat(response.get(0).status()).isEqualTo(OrderStatus.RECEIVED);
            verify(orderRepository, never()).findAllByOrderByCreatedAtDesc();
        }

        @Test
        void shouldListAllOrders_whenOrderStatusIsNotPresent() {
            Order receivedOrder = buildOrder(OrderStatus.RECEIVED);
            Order deliveredOrder = buildOrder(OrderStatus.DELIVERED);
            when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(receivedOrder, deliveredOrder));

            List<OrderSummaryResponseDTO> response = orderService.listOrders(null);

            assertThat(response).hasSize(2);
            assertThat(response.get(0).status()).isEqualTo(OrderStatus.RECEIVED);
            assertThat(response.get(1).status()).isEqualTo(OrderStatus.DELIVERED);
            verify(orderRepository, never()).findByStatusOrderByCreatedAtDesc(any());
        }

    }

    @Nested
    class FindOrderById {

        @Test
        void shouldReturnOrder_whenOrderExists() {
            Order order = buildOrder(OrderStatus.RECEIVED);
            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            OrderResponseDTO response = orderService.findOrderById(10L);

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.customerId()).isEqualTo(1L);
            assertThat(response.status()).isEqualTo(OrderStatus.RECEIVED);
        }

        @Test
        void shouldThrowResourceNotFoundException_whenOrderDoesNotExist() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> orderService.findOrderById(99L));
        }
    }

    @Nested
    class CreateOrder {

        @Test
        void shouldCreateOrderWithCalculatedTotal_whenRequestIsValid() {
            MenuItem burger = buildMenuItem(1L, "Hambúrguer", "32.00", true);
            MenuItem soda = buildMenuItem(3L, "Refrigerante", "7.00", true);
            OrderRequestDTO request = new OrderRequestDTO(1L, "Sem cebola", List.of(
                    new OrderItemRequestDTO(1L, 2),
                    new OrderItemRequestDTO(3L, 1)));
            when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(menuItemRepository.findById(1L)).thenReturn(Optional.of(burger));
            when(menuItemRepository.findById(3L)).thenReturn(Optional.of(soda));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            OrderResponseDTO response = orderService.createOrder(request);

            assertThat(response.total()).isEqualByComparingTo("71.00");
            assertThat(response.notes()).isEqualTo("Sem cebola");
            assertThat(response.items()).hasSize(2);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();

            assertThat(savedOrder.getCustomer()).isEqualTo(customer);
            OrderItem burgerItem = savedOrder.getItems().get(0);
            assertThat(burgerItem.getUnitPrice()).isEqualByComparingTo("32.00");
            assertThat(burgerItem.getQuantity()).isEqualTo(2);
            assertThat(burgerItem.getSubtotal()).isEqualByComparingTo("64.00");
            assertThat(burgerItem.getOrder()).isEqualTo(savedOrder);
        }

        @Test
        void shouldThrowResourceNotFoundException_whenCustomerDoesNotExist() {
            OrderRequestDTO request = new OrderRequestDTO(99L, null, List.of(new OrderItemRequestDTO(1L, 1)));
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> orderService.createOrder(request));

            assertThat(exception.getMessage()).isEqualTo("Cliente não encontrado");
            verify(menuItemRepository, never()).findById(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        void shouldThrowResourceNotFoundException_whenMenuItemDoesNotExist() {
            OrderRequestDTO request = new OrderRequestDTO(1L, null, List.of(new OrderItemRequestDTO(99L, 1)));
            when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> orderService.createOrder(request));

            assertThat(exception.getMessage()).contains("99");
            verify(orderRepository, never()).save(any());
        }

        @Test
        void shouldThrowBusinessRuleException_whenMenuItemIsUnavailable() {
            MenuItem burger = buildMenuItem(1L, "Hambúrguer", "32.00", false);
            OrderRequestDTO request = new OrderRequestDTO(1L, null, List.of(new OrderItemRequestDTO(1L, 1)));
            when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(menuItemRepository.findById(1L)).thenReturn(Optional.of(burger));

            BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                    () -> orderService.createOrder(request));

            assertThat(exception.getMessage()).contains("Hambúrguer");
            verify(orderRepository, never()).save(any());
        }

        @Test
        void shouldThrowBusinessRuleException_whenMenuItemIsRepeated() {
            OrderRequestDTO request = new OrderRequestDTO(1L, null, List.of(
                    new OrderItemRequestDTO(1L, 2),
                    new OrderItemRequestDTO(1L, 1)));
            when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

            BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                    () -> orderService.createOrder(request));

            assertThat(exception.getMessage()).contains("mais de uma vez");
            verify(menuItemRepository, never()).findById(any());
            verify(orderRepository, never()).save(any());
        }
    }
}
