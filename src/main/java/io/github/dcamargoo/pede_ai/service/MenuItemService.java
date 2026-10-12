package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.MenuItemRequestDTO;
import io.github.dcamargoo.pede_ai.dto.MenuItemResponseDTO;
import io.github.dcamargoo.pede_ai.exception.NotImplementedException;
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
        throw new NotImplementedException("NI");
    }

    @Transactional
    public MenuItemResponseDTO updateItem(Long id, MenuItemRequestDTO request){
        throw new NotImplementedException("NI");
    }

    @Transactional
    public MenuItemResponseDTO updateAvailability(Long id, Boolean available){
        throw new NotImplementedException("NI");
    }

    @Transactional
    public void deleteItem(Long id){
        throw new NotImplementedException("NI");
    }

    @Transactional(readOnly = true)
    public MenuItemResponseDTO findById(Long id){
        throw new NotImplementedException("NI");
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> listItems(Long categoryId, Boolean available){
        throw new NotImplementedException("NI");
    }

}
