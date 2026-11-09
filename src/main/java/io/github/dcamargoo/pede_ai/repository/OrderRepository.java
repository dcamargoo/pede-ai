package io.github.dcamargoo.pede_ai.repository;

import io.github.dcamargoo.pede_ai.entity.Order;
import io.github.dcamargoo.pede_ai.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.menuItem"})
    Optional<Order> findWithItemsById(Long id);

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
