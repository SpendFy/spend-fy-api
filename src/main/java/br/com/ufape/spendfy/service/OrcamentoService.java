package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.orcamento.OrcamentoDTO;
import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.Orcamento;
import br.com.ufape.spendfy.entity.User;
import br.com.ufape.spendfy.exception.InvalidOperationException;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.mapper.EntityMapper;
import br.com.ufape.spendfy.repository.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrcamentoService {
    
    private final OrcamentoRepository orcamentoRepository;
    private final CategoriaService categoriaService;
    private final UserService userService;
    private final EntityMapper mapper;
    
    public OrcamentoDTO createOrcamento(OrcamentoDTO dto, String userId) {
        User user = userService.findUserById(userId);
        Categoria categoria = categoriaService.findCategoriaById(dto.getCategoriaId());
        
        validateDateRange(dto.getStartDate(), dto.getEndDate());
        checkOverlappingBudgets(userId, dto.getCategoriaId(), dto.getStartDate(), dto.getEndDate());
        
        Orcamento orcamento = mapper.toOrcamentoEntity(dto, categoria, user);
        Orcamento savedOrcamento = orcamentoRepository.save(orcamento);
        return mapper.toOrcamentoDTO(savedOrcamento);
    }
    
    @Transactional(readOnly = true)
    public OrcamentoDTO getOrcamentoById(String id, String userId) {
        Orcamento orcamento = orcamentoRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
        return mapper.toOrcamentoDTO(orcamento);
    }
    
    @Transactional(readOnly = true)
    public List<OrcamentoDTO> getAllOrcamentosByUser(String userId) {
        userService.findUserById(userId);
        return orcamentoRepository.findByUserId(userId)
            .stream()
            .map(mapper::toOrcamentoDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<OrcamentoDTO> getOrcamentosByCategoria(String userId, String categoriaId) {
        userService.findUserById(userId);
        categoriaService.findCategoriaById(categoriaId);
        return orcamentoRepository.findByUserIdAndCategoriaId(userId, categoriaId)
            .stream()
            .map(mapper::toOrcamentoDTO)
            .collect(Collectors.toList());
    }
    
    public OrcamentoDTO updateOrcamento(String id, OrcamentoDTO dto, String userId) {
        Orcamento orcamento = orcamentoRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
        
        validateDateRange(dto.getStartDate(), dto.getEndDate());
        
        if (!orcamento.getStartDate().equals(dto.getStartDate()) || 
            !orcamento.getEndDate().equals(dto.getEndDate())) {
            checkOverlappingBudgets(userId, dto.getCategoriaId(), dto.getStartDate(), dto.getEndDate());
        }
        
        Categoria categoria = categoriaService.findCategoriaById(dto.getCategoriaId());
        
        orcamento.setName(dto.getName());
        orcamento.setLimitAmount(dto.getLimitAmount());
        orcamento.setStartDate(dto.getStartDate());
        orcamento.setEndDate(dto.getEndDate());
        orcamento.setCategoria(categoria);
        orcamento.setDescription(dto.getDescription());
        
        Orcamento updatedOrcamento = orcamentoRepository.save(orcamento);
        return mapper.toOrcamentoDTO(updatedOrcamento);
    }
    
    public void deleteOrcamento(String id, String userId) {
        Orcamento orcamento = orcamentoRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
        orcamentoRepository.delete(orcamento);
    }
    
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidOperationException("Start date must be before end date");
        }
    }
    
    private void checkOverlappingBudgets(String userId, String categoriaId, LocalDate startDate, LocalDate endDate) {
        List<Orcamento> overlapping = orcamentoRepository.findOverlappingBudgets(userId, categoriaId, startDate, endDate);
        if (!overlapping.isEmpty()) {
            throw new InvalidOperationException("Budget period overlaps with existing budget for this category");
        }
    }
}