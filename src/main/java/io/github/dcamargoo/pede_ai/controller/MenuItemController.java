package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.dto.MenuItemAvailabilityRequestDTO;
import io.github.dcamargoo.pede_ai.dto.MenuItemRequestDTO;
import io.github.dcamargoo.pede_ai.dto.MenuItemResponseDTO;
import io.github.dcamargoo.pede_ai.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu-items")
@Tag(name = "Itens do cardápio", description = "Itens do cardápio. Consultar é público, o resto é só para o restaurante")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService){
        this.menuItemService = menuItemService;
    }

    @Operation(summary = "Cria um item do cardápio")
    @ApiResponse(responseCode = "201", description = "Item criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas o restaurante pode criar itens")
    @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    @PostMapping
    public ResponseEntity<MenuItemResponseDTO> create(@Valid @RequestBody MenuItemRequestDTO request){
        MenuItemResponseDTO response = menuItemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualiza um item do cardápio")
    @ApiResponse(responseCode = "200", description = "Item atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas o restaurante pode atualizar itens")
    @ApiResponse(responseCode = "404", description = "Item ou categoria não encontrados")
    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> update(@PathVariable Long id, @Valid @RequestBody MenuItemRequestDTO request){
        MenuItemResponseDTO response = menuItemService.updateItem(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Altera a disponibilidade de um item")
    @ApiResponse(responseCode = "200", description = "Disponibilidade atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas o restaurante pode alterar a disponibilidade")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    @PatchMapping("/{id}/availability")
    public ResponseEntity<MenuItemResponseDTO> updateAvailability(@PathVariable Long id, @Valid @RequestBody MenuItemAvailabilityRequestDTO request){
        MenuItemResponseDTO response = menuItemService.updateAvailability(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove um item do cardápio")
    @ApiResponse(responseCode = "204", description = "Item removido")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas o restaurante pode remover itens")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    @ApiResponse(responseCode = "409", description = "O item está em um pedido e não pode ser removido")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        menuItemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca um item do cardápio pelo id")
    @ApiResponse(responseCode = "200", description = "Item encontrado")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    @SecurityRequirements
    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO> findById(@PathVariable Long id){
        MenuItemResponseDTO response = menuItemService.findById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lista os itens do cardápio", description = "Aceita filtro por categoria e por disponibilidade")
    @ApiResponse(responseCode = "200", description = "Lista de itens")
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<List<MenuItemResponseDTO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean available) {
        List<MenuItemResponseDTO> response = menuItemService.listItems(categoryId, available);
        return ResponseEntity.ok(response);
    }

}
