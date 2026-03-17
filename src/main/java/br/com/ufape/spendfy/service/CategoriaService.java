package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.categoria.CategoriaDTO;
import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.CategoriaType;
import br.com.ufape.spendfy.entity.User;
import br.com.ufape.spendfy.exception.DuplicateResourceException;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.mapper.EntityMapper;
import br.com.ufape.spendfy.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaService {
    
    private final CategoriaRepository categoriaRepository;
    private final UserService userService;
    private final EntityMapper mapper;
    
    public CategoriaDTO createCategoria(CategoriaDTO dto, String userId) {
        User user = userService.findUserById(userId);
        
        if (categoriaRepository.existsByUserIdAndName(userId, dto.getName())) {
            throw new DuplicateResourceException("Category with name already exists for this user: " + dto.getName());
        }
        
        Categoria categoria = mapper.toCategoriaEntity(dto, user);
        Categoria savedCategoria = categoriaRepository.save(categoria);
        return mapper.toCategoriaDTO(savedCategoria);
    }
    
    @Transactional(readOnly = true)
    public CategoriaDTO getCategoriaById(String id, String userId) {
        Categoria categoria = categoriaRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapper.toCategoriaDTO(categoria);
    }
    
    @Transactional(readOnly = true)
    public List<CategoriaDTO> getAllCategoriasByUser(String userId) {
        userService.findUserById(userId);
        return categoriaRepository.findByUserId(userId)
            .stream()
            .map(mapper::toCategoriaDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CategoriaDTO> getCategoriasByType(String userId, String type) {
        userService.findUserById(userId);
        CategoriaType categoriaType = CategoriaType.valueOf(type);
        return categoriaRepository.findByUserIdAndType(userId, categoriaType)
            .stream()
            .map(mapper::toCategoriaDTO)
            .collect(Collectors.toList());
    }
    
    public CategoriaDTO updateCategoria(String id, CategoriaDTO dto, String userId) {
        Categoria categoria = categoriaRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        if (!categoria.getName().equals(dto.getName()) && 
            categoriaRepository.existsByUserIdAndName(userId, dto.getName())) {
            throw new DuplicateResourceException("Category with name already exists: " + dto.getName());
        }
        
        categoria.setName(dto.getName());
        categoria.setDescription(dto.getDescription());
        categoria.setColor(dto.getColor());
        categoria.setType(CategoriaType.valueOf(dto.getType()));
        
        Categoria updatedCategoria = categoriaRepository.save(categoria);
        return mapper.toCategoriaDTO(updatedCategoria);
    }
    
    public void deleteCategoria(String id, String userId) {
        Categoria categoria = categoriaRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoriaRepository.delete(categoria);
    }
    
    public Categoria findCategoriaById(String id) {
        return categoriaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }
}