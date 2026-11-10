package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.dto.CategoryRequestDTO;
import io.github.dcamargoo.pede_ai.dto.CategoryResponseDTO;
import io.github.dcamargoo.pede_ai.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
@Tag(name = "Categorias", description = "Categorias do cardápio. Listar é público, o resto é só para o restaurante")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @Operation(summary = "Cria uma categoria")
    @ApiResponse(responseCode = "201", description = "Categoria criada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas o restaurante pode criar categorias")
    @ApiResponse(responseCode = "409", description = "Já existe uma categoria com esse nome")
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CategoryRequestDTO request){
        CategoryResponseDTO response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualiza uma categoria")
    @ApiResponse(responseCode = "200", description = "Categoria atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas o restaurante pode atualizar categorias")
    @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    @ApiResponse(responseCode = "409", description = "Já existe uma categoria com esse nome")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CategoryRequestDTO request){
        CategoryResponseDTO response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove uma categoria")
    @ApiResponse(responseCode = "204", description = "Categoria removida")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas o restaurante pode remover categorias")
    @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    @ApiResponse(responseCode = "409", description = "A categoria possui itens e não pode ser removida")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lista todas as categorias")
    @ApiResponse(responseCode = "200", description = "Lista de categorias")
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> list(){
        return ResponseEntity.ok(categoryService.listCategories());
    }

}
