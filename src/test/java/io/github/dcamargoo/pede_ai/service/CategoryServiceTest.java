package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.CategoryRequestDTO;
import io.github.dcamargoo.pede_ai.dto.CategoryResponseDTO;
import io.github.dcamargoo.pede_ai.entity.Category;
import io.github.dcamargoo.pede_ai.exception.ConflictException;
import io.github.dcamargoo.pede_ai.exception.ResourceNotFoundException;
import io.github.dcamargoo.pede_ai.repository.CategoryRepository;
import io.github.dcamargoo.pede_ai.repository.MenuItemRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category buildCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    @Nested
    class ListCategories {

        @Test
        void shouldReturnAllCategories() {
            Category snacks = buildCategory(1L, "Lanches");
            Category drinks = buildCategory(2L, "Bebidas");
            when(categoryRepository.findAll()).thenReturn(List.of(snacks, drinks));

            List<CategoryResponseDTO> response = categoryService.listCategories();

            assertThat(response).hasSize(2);
            assertThat(response.get(0).name()).isEqualTo("Lanches");
            assertThat(response.get(1).name()).isEqualTo("Bebidas");
        }
    }

    @Nested
    class CreateCategory {

        @Test
        void shouldCreateCategory_whenNameIsNotInUse() {
            CategoryRequestDTO request = new CategoryRequestDTO("Lanches");
            when(categoryRepository.existsByName("Lanches")).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(buildCategory(1L, "Lanches"));

            CategoryResponseDTO response = categoryService.createCategory(request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Lanches");
        }

        @Test
        void shouldThrowConflictException_whenNameIsAlreadyInUse() {
            CategoryRequestDTO request = new CategoryRequestDTO("Lanches");
            when(categoryRepository.existsByName("Lanches")).thenReturn(true);

            assertThrows(ConflictException.class, () -> categoryService.createCategory(request));

            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateCategory {

        @Test
        void shouldUpdateCategory_whenNameIsNotUsedByAnotherCategory() {
            Category category = buildCategory(1L, "Lanches");
            CategoryRequestDTO request = new CategoryRequestDTO("Sanduíches");
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameAndIdNot("Sanduíches", 1L)).thenReturn(false);
            when(categoryRepository.save(category)).thenReturn(category);

            CategoryResponseDTO response = categoryService.updateCategory(1L, request);

            assertThat(response.name()).isEqualTo("Sanduíches");
            verify(categoryRepository).save(category);
        }

        @Test
        void shouldThrowConflictException_whenNameIsUsedByAnotherCategory() {
            Category category = buildCategory(1L, "Lanches");
            CategoryRequestDTO request = new CategoryRequestDTO("Bebidas");
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameAndIdNot("Bebidas", 1L)).thenReturn(true);

            assertThrows(ConflictException.class, () -> categoryService.updateCategory(1L, request));

            assertThat(category.getName()).isEqualTo("Lanches");
            verify(categoryRepository, never()).save(any());
        }

        @Test
        void shouldThrowResourceNotFoundException_whenCategoryDoesNotExist() {
            CategoryRequestDTO request = new CategoryRequestDTO("Lanches");
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(99L, request));

            verify(categoryRepository, never()).existsByNameAndIdNot(anyString(), anyLong());
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteCategory {

        @Test
        void shouldDeleteCategory_whenCategoryHasNoItems() {
            when(categoryRepository.existsById(1L)).thenReturn(true);
            when(menuItemRepository.existsByCategoryId(1L)).thenReturn(false);

            categoryService.deleteCategory(1L);

            verify(categoryRepository).deleteById(1L);
        }

        @Test
        void shouldThrowConflictException_whenCategoryHasItems() {
            when(categoryRepository.existsById(1L)).thenReturn(true);
            when(menuItemRepository.existsByCategoryId(1L)).thenReturn(true);

            assertThrows(ConflictException.class, () -> categoryService.deleteCategory(1L));

            verify(categoryRepository, never()).deleteById(any());
        }

        @Test
        void shouldThrowResourceNotFoundException_whenCategoryDoesNotExist() {
            when(categoryRepository.existsById(99L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(99L));

            verify(categoryRepository, never()).deleteById(any());
        }
    }
}
