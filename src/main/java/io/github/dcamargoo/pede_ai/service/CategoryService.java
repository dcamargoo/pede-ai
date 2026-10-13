package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.CategoryRequestDTO;
import io.github.dcamargoo.pede_ai.dto.CategoryResponseDTO;
import io.github.dcamargoo.pede_ai.entity.Category;
import io.github.dcamargoo.pede_ai.exception.ConflictException;
import io.github.dcamargoo.pede_ai.exception.ResourceNotFoundException;
import io.github.dcamargoo.pede_ai.repository.CategoryRepository;
import io.github.dcamargoo.pede_ai.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    public CategoryService(CategoryRepository categoryRepository, MenuItemRepository menuItemRepository){
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> listCategories(){
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request){
        boolean exists = categoryRepository.existsByName(request.name());
        if (exists){
            throw new ConflictException("Essa categoria já existe");
        }

        Category newCategory = new Category();
        newCategory.setName(request.name());

        Category savedCategory = categoryRepository.save(newCategory);
        return toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request) {

        // 1. Busca a categoria pelo id. Se não existir, para aqui com erro.
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        // 2. Verifica se OUTRA categoria já usa esse nome.
        boolean nameInUse = categoryRepository.existsByNameAndIdNot(request.name(), id);
        if (nameInUse) {
            throw new ConflictException("Já existe uma categoria com esse nome");
        }

        // 3. Troca o nome da categoria encontrada pelo nome novo.
        category.setName(request.name());

        Category savedCategory = categoryRepository.save(category);
        return toResponse(savedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {

        // 1. Se a categoria não existe, avisa com 404.
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria não encontrada");
        }

        // 2. Se a categoria tem itens, não pode ser removida (RN12).
        if (menuItemRepository.existsByCategoryId(id)) {
            throw new ConflictException("Não é possível remover uma categoria que possui itens");
        }

        categoryRepository.deleteById(id);
    }

    private CategoryResponseDTO toResponse(Category category){
        return new CategoryResponseDTO(
                category.getId(),
                category.getName());
    }

}
