package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.MenuItemAvailabilityRequestDTO;
import io.github.dcamargoo.pede_ai.dto.MenuItemRequestDTO;
import io.github.dcamargoo.pede_ai.dto.MenuItemResponseDTO;
import io.github.dcamargoo.pede_ai.entity.Category;
import io.github.dcamargoo.pede_ai.entity.MenuItem;
import io.github.dcamargoo.pede_ai.exception.ConflictException;
import io.github.dcamargoo.pede_ai.exception.ResourceNotFoundException;
import io.github.dcamargoo.pede_ai.repository.CategoryRepository;
import io.github.dcamargoo.pede_ai.repository.MenuItemRepository;
import io.github.dcamargoo.pede_ai.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class MenuItemServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private MenuItemService menuItemService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Lanches");
    }

    private MenuItem buildMenuItem() {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(10L);
        menuItem.setName("Hambúrguer");
        menuItem.setDescription("Pão, carne e queijo");
        menuItem.setPrice(new BigDecimal("32.00"));
        menuItem.setAvailable(true);
        menuItem.setCategory(category);
        return menuItem;
    }

    private MenuItemRequestDTO buildRequest(Long categoryId) {
        return new MenuItemRequestDTO("Hambúrguer", "Pão, carne e queijo", new BigDecimal("32.00"), categoryId);
    }

    @Nested
    class CreateItem {

        @Test
        void shouldCreateAvailableItem_whenCategoryExists() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            MenuItemResponseDTO response = menuItemService.createItem(buildRequest(1L));

            assertThat(response.name()).isEqualTo("Hambúrguer");
            assertThat(response.price()).isEqualByComparingTo("32.00");
            assertThat(response.available()).isTrue();
            assertThat(response.category().name()).isEqualTo("Lanches");

            ArgumentCaptor<MenuItem> menuItemCaptor = ArgumentCaptor.forClass(MenuItem.class);
            verify(menuItemRepository).save(menuItemCaptor.capture());
            assertThat(menuItemCaptor.getValue().getCategory()).isEqualTo(category);
        }

        @Test
        void shouldThrowResourceNotFoundException_whenCategoryDoesNotExist() {
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> menuItemService.createItem(buildRequest(99L)));

            verify(menuItemRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateItem {

        @Test
        void shouldUpdateItem_whenItemAndCategoryExist() {
            MenuItem menuItem = buildMenuItem();
            MenuItemRequestDTO request = new MenuItemRequestDTO("X-Burger", "Pão, carne, queijo e bacon",
                    new BigDecimal("35.00"), 1L);
            when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(menuItemRepository.save(menuItem)).thenReturn(menuItem);

            MenuItemResponseDTO response = menuItemService.updateItem(10L, request);

            assertThat(response.name()).isEqualTo("X-Burger");
            assertThat(response.description()).isEqualTo("Pão, carne, queijo e bacon");
            assertThat(response.price()).isEqualByComparingTo("35.00");
        }

        @Test
        void shouldThrowResourceNotFoundException_whenItemDoesNotExist() {
            when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> menuItemService.updateItem(99L, buildRequest(1L)));

            verify(menuItemRepository, never()).save(any());
        }

        @Test
        void shouldThrowResourceNotFoundException_whenCategoryDoesNotExist() {
            MenuItem menuItem = buildMenuItem();
            when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> menuItemService.updateItem(10L, buildRequest(99L)));

            verify(menuItemRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateAvailability {

        @Test
        void shouldMarkItemAsUnavailable_whenItemExists() {
            MenuItem menuItem = buildMenuItem();
            when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));
            when(menuItemRepository.save(menuItem)).thenReturn(menuItem);

            MenuItemResponseDTO response = menuItemService.updateAvailability(10L,
                    new MenuItemAvailabilityRequestDTO(false));

            assertThat(response.available()).isFalse();
            verify(menuItemRepository).save(menuItem);
        }

        @Test
        void shouldThrowResourceNotFoundException_whenItemDoesNotExist() {
            when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> menuItemService.updateAvailability(99L, new MenuItemAvailabilityRequestDTO(false)));

            verify(menuItemRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteItem {

        @Test
        void shouldDeleteItem_whenItemWasNeverOrdered() {
            MenuItem menuItem = buildMenuItem();
            when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));
            when(orderItemRepository.existsByMenuItemId(10L)).thenReturn(false);

            menuItemService.deleteItem(10L);

            verify(menuItemRepository).delete(menuItem);
        }

        @Test
        void shouldThrowConflictException_whenItemWasAlreadyOrdered() {
            MenuItem menuItem = buildMenuItem();
            when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));
            when(orderItemRepository.existsByMenuItemId(10L)).thenReturn(true);

            assertThrows(ConflictException.class, () -> menuItemService.deleteItem(10L));

            verify(menuItemRepository, never()).delete(any());
        }

        @Test
        void shouldThrowResourceNotFoundException_whenItemDoesNotExist() {
            when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> menuItemService.deleteItem(99L));

            verify(menuItemRepository, never()).delete(any());
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnItem_whenItemExists() {
            MenuItem menuItem = buildMenuItem();
            when(menuItemRepository.findById(10L)).thenReturn(Optional.of(menuItem));

            MenuItemResponseDTO response = menuItemService.findById(10L);

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.name()).isEqualTo("Hambúrguer");
            assertThat(response.category().id()).isEqualTo(1L);
        }

        @Test
        void shouldThrowResourceNotFoundException_whenItemDoesNotExist() {
            when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> menuItemService.findById(99L));
        }
    }

    @Nested
    class ListItems {

        @Test
        void shouldFilterByCategoryAndAvailability_whenBothAreInformed() {
            when(menuItemRepository.findByCategoryIdAndAvailable(1L, true)).thenReturn(List.of(buildMenuItem()));

            List<MenuItemResponseDTO> response = menuItemService.listItems(1L, true);

            assertThat(response).hasSize(1);
        }

        @Test
        void shouldFilterByCategory_whenOnlyCategoryIsInformed() {
            when(menuItemRepository.findByCategoryId(1L)).thenReturn(List.of(buildMenuItem()));

            List<MenuItemResponseDTO> response = menuItemService.listItems(1L, null);

            assertThat(response).hasSize(1);
        }

        @Test
        void shouldFilterByAvailability_whenOnlyAvailabilityIsInformed() {
            when(menuItemRepository.findByAvailable(true)).thenReturn(List.of(buildMenuItem()));

            List<MenuItemResponseDTO> response = menuItemService.listItems(null, true);

            assertThat(response).hasSize(1);
        }

        @Test
        void shouldReturnAllItems_whenNoFilterIsInformed() {
            when(menuItemRepository.findAll()).thenReturn(List.of(buildMenuItem()));

            List<MenuItemResponseDTO> response = menuItemService.listItems(null, null);

            assertThat(response).hasSize(1);
        }
    }
}
