package io.github.dcamargoo.pede_ai.repository;

import io.github.dcamargoo.pede_ai.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
