package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.CategoriaRequestDTO;
import io.github.dcamargoo.pede_ai.dto.CategoriaResponseDTO;
import io.github.dcamargoo.pede_ai.entity.Categoria;
import io.github.dcamargoo.pede_ai.exception.ConflitoException;
import io.github.dcamargoo.pede_ai.exception.RecursoNaoEncontradoException;
import io.github.dcamargoo.pede_ai.repository.CategoriaRepository;
import io.github.dcamargoo.pede_ai.repository.ItemCardapioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ItemCardapioRepository itemCardapioRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ItemCardapioRepository itemCardapioRepository){
        this.categoriaRepository = categoriaRepository;
        this.itemCardapioRepository = itemCardapioRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarCategorias(){
        return categoriaRepository.findAll()
                .stream()
                .map(this::toResponseCategoria)
                .toList();
    }

    @Transactional
    public CategoriaResponseDTO criarCategoria(CategoriaRequestDTO request){
        boolean existe = categoriaRepository.existsByNome(request.nome());
        if (existe){
            throw new ConflitoException("Essa categoria já existe");
        }
        Categoria categoriaNova = new Categoria();
        categoriaNova.setNome(request.nome());
        Categoria categoriaSalva = categoriaRepository.save(categoriaNova);
        return toResponseCategoria(categoriaSalva);
    }

    @Transactional
    public CategoriaResponseDTO atualizarCategoria(Long id, CategoriaRequestDTO request) {

        // 1. Busca a categoria pelo id. Se não existir, para aqui com erro.
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));

        // 2. Verifica se OUTRA categoria já usa esse nome.
        boolean nomeEmUso = categoriaRepository.existsByNomeAndIdNot(request.nome(), id);
        if (nomeEmUso) {
            throw new ConflitoException("Já existe uma categoria com esse nome");
        }

        // 3. Troca o nome da categoria encontrada pelo nome novo.
        categoria.setNome(request.nome());

        Categoria categoriaSalva = categoriaRepository.save(categoria);
        return toResponseCategoria(categoriaSalva);
    }

    @Transactional
    public void removerCategoria(Long id) {

        // 1. Se a categoria não existe, avisa com 404.
        if (!categoriaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Categoria não encontrada");
        }

        // 2. Se a categoria tem itens, não pode ser removida (RN12).
        if (itemCardapioRepository.existsByCategoriaId(id)) {
            throw new ConflitoException("Não é possível remover uma categoria que possui itens");
        }

        categoriaRepository.deleteById(id);
    }

    private CategoriaResponseDTO toResponseCategoria(Categoria categoria){
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNome());
    }

}
