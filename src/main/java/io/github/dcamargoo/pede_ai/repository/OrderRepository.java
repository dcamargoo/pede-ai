package io.github.dcamargoo.pede_ai.repository;

import io.github.dcamargoo.pede_ai.entity.Order;
import io.github.dcamargoo.pede_ai.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.menuItem"})
    Optional<Order> findWithItemsById(Long id);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
}
