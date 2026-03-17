package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.conta.ContaDTO;
import br.com.ufape.spendfy.entity.Conta;
import br.com.ufape.spendfy.entity.ContaType;
import br.com.ufape.spendfy.entity.User;
import br.com.ufape.spendfy.exception.DuplicateResourceException;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.mapper.EntityMapper;
import br.com.ufape.spendfy.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContaService {
    
    private final ContaRepository contaRepository;
    private final UserService userService;
    private final EntityMapper mapper;
    
    public ContaDTO createConta(ContaDTO dto, String userId) {
        User user = userService.findUserById(userId);
        
        if (contaRepository.existsByUserIdAndName(userId, dto.getName())) {
            throw new DuplicateResourceException("Account with name already exists: " + dto.getName());
        }
        
        Conta conta = mapper.toContaEntity(dto, user);
        if (conta.getInitialBalance() != null) {
            conta.setBalance(conta.getInitialBalance());
        }
        
        Conta savedConta = contaRepository.save(conta);
        return mapper.toContaDTO(savedConta);
    }
    
    @Transactional(readOnly = true)
    public ContaDTO getContaById(String id, String userId) {
        Conta conta = contaRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        return mapper.toContaDTO(conta);
    }
    
    @Transactional(readOnly = true)
    public List<ContaDTO> getAllContasByUser(String userId) {
        userService.findUserById(userId);
        return contaRepository.findByUserId(userId)
            .stream()
            .map(mapper::toContaDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ContaDTO> getActiveContasByUser(String userId) {
        userService.findUserById(userId);
        return contaRepository.findByUserIdAndActiveTrue(userId)
            .stream()
            .map(mapper::toContaDTO)
            .collect(Collectors.toList());
    }
    
    public ContaDTO updateConta(String id, ContaDTO dto, String userId) {
        Conta conta = contaRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        
        if (!conta.getName().equals(dto.getName()) && 
            contaRepository.existsByUserIdAndName(userId, dto.getName())) {
            throw new DuplicateResourceException("Account with name already exists: " + dto.getName());
        }
        
        conta.setName(dto.getName());
        conta.setDescription(dto.getDescription());
        conta.setType(ContaType.valueOf(dto.getType()));
        conta.setActive(dto.getActive() != null ? dto.getActive() : true);
        
        Conta updatedConta = contaRepository.save(conta);
        return mapper.toContaDTO(updatedConta);
    }
    
    public void deleteConta(String id, String userId) {
        Conta conta = contaRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        conta.setActive(false);
        contaRepository.save(conta);
    }
    
    public Conta findContaById(String id) {
        return contaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }
}