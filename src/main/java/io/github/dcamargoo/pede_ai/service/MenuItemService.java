package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.CategoryResponseDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    public MenuItemService(MenuItemRepository menuItemRepository,
                               CategoryRepository categoryRepository,
                               OrderItemRepository orderItemRepository) {
        this.menuItemRepository = menuItemRepository;
        this.categoryRepository = categoryRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public MenuItemResponseDTO createItem(MenuItemRequestDTO request){
        MenuItem newMenuItem = new MenuItem();
        Category category = this.getCategoryIfExists(request.categoryId());

        newMenuItem.setName(request.name());
        newMenuItem.setDescription(request.description());
        newMenuItem.setPrice(request.price());
        newMenuItem.setCategory(category);

        MenuItem savedMenuItem = menuItemRepository.save(newMenuItem);
        return toResponse(savedMenuItem);
    }

    @Transactional
    public MenuItemResponseDTO updateItem(Long id, MenuItemRequestDTO request){
        MenuItem menuItem = this.getMenuItemIfExists(id);
        Category category = this.getCategoryIfExists(request.categoryId());

        menuItem.setName(request.name());
        menuItem.setDescription(request.description());
        menuItem.setPrice(request.price());
        menuItem.setCategory(category);

        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        return toResponse(updatedMenuItem);
    }

    @Transactional
    public MenuItemResponseDTO updateAvailability(Long id, MenuItemAvailabilityRequestDTO request){
        MenuItem menuItem = this.getMenuItemIfExists(id);

        menuItem.setAvailable(request.available());

        MenuItem menuItemUpdated = menuItemRepository.save(menuItem);
        return toResponse(menuItemUpdated);
    }

    @Transactional
    public void deleteItem(Long id){
        MenuItem menuItem = getMenuItemIfExists(id);

        if(orderItemRepository.existsByMenuItemId(id)){
            throw new ConflictException("Não é possível remover um item do cardápio que se encontra em um pedido");
        }

        menuItemRepository.delete(menuItem);
    }

    @Transactional(readOnly = true)
    public MenuItemResponseDTO findById(Long id){
        MenuItem menuItem = this.getMenuItemIfExists(id);
        return toResponse(menuItem);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> listItems(Long categoryId, Boolean available) {
        List<MenuItem> items;

        if (categoryId != null && available != null) {
            items = menuItemRepository.findByCategoryIdAndAvailable(categoryId, available);
        } else if (categoryId != null) {
            items = menuItemRepository.findByCategoryId(categoryId);
        } else if (available != null) {
            items = menuItemRepository.findByAvailable(available);
        } else {
            items = menuItemRepository.findAll();
        }

        return items.stream()
                .map(this::toResponse)
                .toList();
    }

    private MenuItemResponseDTO toResponse(MenuItem item) {
        return new MenuItemResponseDTO(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getAvailable(),
                new CategoryResponseDTO(
                        item.getCategory().getId(),
                        item.getCategory().getName()
                )
        );
    }

    private MenuItem getMenuItemIfExists(Long id){
        return menuItemRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado"));
    }

    private Category getCategoryIfExists(Long categoryId){
        return categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }
}
