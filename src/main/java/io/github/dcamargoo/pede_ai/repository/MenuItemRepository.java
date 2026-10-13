package io.github.dcamargoo.pede_ai.repository;

import io.github.dcamargoo.pede_ai.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    boolean existsByCategoryId(Long categoryId);

    List<MenuItem> findByCategoryId(Long categoryId);
    List<MenuItem> findByAvailable(Boolean available);
    List<MenuItem> findByCategoryIdAndAvailable(Long categoryId, Boolean available);
}
