package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.transacao.TransacaoDTO;
import br.com.ufape.spendfy.entity.Conta;
import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.Transacao;
import br.com.ufape.spendfy.entity.TipoTransacao;
import br.com.ufape.spendfy.exception.InvalidOperationException;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.mapper.EntityMapper;
import br.com.ufape.spendfy.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransacaoService {
    
    private final TransacaoRepository transacaoRepository;
    private final ContaService contaService;
    private final CategoriaService categoriaService;
    private final UserService userService;
    private final EntityMapper mapper;
    
    public TransacaoDTO createTransacao(TransacaoDTO dto, String userId) {
        userService.findUserById(userId);
        Conta conta = contaService.findContaById(dto.getContaId());
        Categoria categoria = categoriaService.findCategoriaById(dto.getCategoriaId());
        
        validateCategoriaType(dto.getType(), categoria);
        
        Transacao transacao = mapper.toTransacaoEntity(dto, conta, categoria, userService.findUserById(userId));
        
        Transacao savedTransacao = transacaoRepository.save(transacao);
        updateAccountBalance(conta, transacao, true);
        
        return mapper.toTransacaoDTO(savedTransacao);
    }
    
    @Transactional(readOnly = true)
    public TransacaoDTO getTransacaoById(String id, String userId) {
        Transacao transacao = transacaoRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return mapper.toTransacaoDTO(transacao);
    }
    
    @Transactional(readOnly = true)
    public Page<TransacaoDTO> getTransacoesByUser(String userId, Pageable pageable) {
        userService.findUserById(userId);
        return transacaoRepository.findByUserId(userId, pageable)
            .map(mapper::toTransacaoDTO);
    }
    
    @Transactional(readOnly = true)
    public List<TransacaoDTO> getTransacoesByConta(String userId, String contaId) {
        userService.findUserById(userId);
        return transacaoRepository.findByUserIdAndContaId(userId, contaId)
            .stream()
            .map(mapper::toTransacaoDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TransacaoDTO> getTransacoesByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        userService.findUserById(userId);
        return transacaoRepository.findByUserIdAndDateRange(userId, startDate, endDate)
            .stream()
            .map(mapper::toTransacaoDTO)
            .collect(Collectors.toList());
    }
    
    public TransacaoDTO updateTransacao(String id, TransacaoDTO dto, String userId) {
        Transacao transacao = transacaoRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        
        if (!transacao.getAmount().equals(dto.getAmount()) || !transacao.getType().name().equals(dto.getType())) {
            updateAccountBalance(transacao.getConta(), transacao, false);
        }
        
        Conta conta = contaService.findContaById(dto.getContaId());
        Categoria categoria = categoriaService.findCategoriaById(dto.getCategoriaId());
        
        validateCategoriaType(dto.getType(), categoria);
        
        transacao.setDescription(dto.getDescription());
        transacao.setAmount(dto.getAmount());
        transacao.setType(TipoTransacao.valueOf(dto.getType()));
        transacao.setTransactionDate(dto.getTransactionDate());
        transacao.setConta(conta);
        transacao.setCategoria(categoria);
        transacao.setNotes(dto.getNotes());
        transacao.setReconciled(dto.getReconciled());
        
        Transacao updatedTransacao = transacaoRepository.save(transacao);
        updateAccountBalance(conta, updatedTransacao, true);
        
        return mapper.toTransacaoDTO(updatedTransacao);
    }
    
    public void deleteTransacao(String id, String userId) {
        Transacao transacao = transacaoRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        updateAccountBalance(transacao.getConta(), transacao, false);
        transacaoRepository.delete(transacao);
    }
    
    private void updateAccountBalance(Conta conta, Transacao transacao, boolean isAdding) {
        if (transacao.getType() == TipoTransacao.INCOME) {
            conta.setBalance(isAdding ? 
                conta.getBalance().add(transacao.getAmount()) : 
                conta.getBalance().subtract(transacao.getAmount()));
        } else if (transacao.getType() == TipoTransacao.EXPENSE) {
            conta.setBalance(isAdding ? 
                conta.getBalance().subtract(transacao.getAmount()) : 
                conta.getBalance().add(transacao.getAmount()));
        }
    }
    
    private void validateCategoriaType(String tipoTransacao, Categoria categoria) {
        if (tipoTransacao.equals("INCOME") && !categoria.getType().name().equals("INCOME")) {
            throw new InvalidOperationException("Invalid category type for income transaction");
        }
        if (tipoTransacao.equals("EXPENSE") && !categoria.getType().name().equals("EXPENSE")) {
            throw new InvalidOperationException("Invalid category type for expense transaction");
        }
    }
}