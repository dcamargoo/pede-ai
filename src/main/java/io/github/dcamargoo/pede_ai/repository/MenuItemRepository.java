package io.github.dcamargoo.pede_ai.repository;

import io.github.dcamargoo.pede_ai.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    boolean existsByCategoryId(Long categoryId);

    @Override
    @EntityGraph(attributePaths = "category")
    List<MenuItem> findAll();

    @EntityGraph(attributePaths = "category")
    List<MenuItem> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = "category")
    List<MenuItem> findByAvailable(Boolean available);

    @EntityGraph(attributePaths = "category")
    List<MenuItem> findByCategoryIdAndAvailable(Long categoryId, Boolean available);
}
