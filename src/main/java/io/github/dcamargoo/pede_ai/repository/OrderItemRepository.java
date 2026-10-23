package io.github.dcamargoo.pede_ai.repository;

import io.github.dcamargoo.pede_ai.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    boolean existsByMenuItemId(Long menuItemId);
}
