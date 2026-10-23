package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.dto.MenuItemAvailabilityRequestDTO;
import io.github.dcamargoo.pede_ai.dto.MenuItemRequestDTO;
import io.github.dcamargoo.pede_ai.dto.MenuItemResponseDTO;
import io.github.dcamargoo.pede_ai.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService){
        this.menuItemService = menuItemService;
    }

    @PostMapping
    public ResponseEntity<MenuItemResponseDTO> create(@Valid @RequestBody MenuItemRequestDTO request){
        MenuItemResponseDTO response = menuItemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> update(@PathVariable Long id, @Valid @RequestBody MenuItemRequestDTO request){
        MenuItemResponseDTO response = menuItemService.updateItem(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<MenuItemResponseDTO> updateAvailability(@PathVariable Long id, @Valid @RequestBody MenuItemAvailabilityRequestDTO request){
        MenuItemResponseDTO response = menuItemService.updateAvailability(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        menuItemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> findById(@PathVariable Long id){
        MenuItemResponseDTO response = menuItemService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MenuItemResponseDTO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean available) {
        List<MenuItemResponseDTO> response = menuItemService.listItems(categoryId, available);
        return ResponseEntity.ok(response);
    }

}
