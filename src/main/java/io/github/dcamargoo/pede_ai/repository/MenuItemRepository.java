package io.github.dcamargoo.pede_ai.repository;

import io.github.dcamargoo.pede_ai.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    boolean existsByCategoryId(Long categoryId);
}
