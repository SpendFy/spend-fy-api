package br.com.ufape.spendfy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.ufape.spendfy.dto.TransactionDTO;
import br.com.ufape.spendfy.entity.*;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public TransactionDTO createTransaction(TransactionDTO transactionDTO, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = accountRepository.findById(transactionDTO.getAccountId())
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Category category = categoryRepository.findById(transactionDTO.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Transaction transaction = Transaction.builder()
            .description(transactionDTO.getDescription())
            .amount(transactionDTO.getAmount())
            .type(Transaction.TransactionType.valueOf(transactionDTO.getType()))
            .transactionDate(transactionDTO.getTransactionDate())
            .notes(transactionDTO.getNotes())
            .user(user)
            .account(account)
            .category(category)
            .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToDTO(saved);
    }

    public List<TransactionDTO> getTransactionsByUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepository.findByUser(user)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionById(Long id, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Unauthorized access to transaction");
        }

        return mapToDTO(transaction);
    }

    public List<TransactionDTO> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepository.findByUserIdAndDateRange(user.getId(), startDate, endDate)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Unauthorized access to transaction");
        }

        Category category = categoryRepository.findById(transactionDTO.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        transaction.setDescription(transactionDTO.getDescription());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setType(Transaction.TransactionType.valueOf(transactionDTO.getType()));
        transaction.setTransactionDate(transactionDTO.getTransactionDate());
        transaction.setNotes(transactionDTO.getNotes());
        transaction.setCategory(category);

        Transaction updated = transactionRepository.save(transaction);
        return mapToDTO(updated);
    }

    public void deleteTransaction(Long id, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Unauthorized access to transaction");
        }

        transactionRepository.delete(transaction);
    }

    private TransactionDTO mapToDTO(Transaction transaction) {
        return TransactionDTO.builder()
            .id(transaction.getId())
            .description(transaction.getDescription())
            .amount(transaction.getAmount())
            .type(transaction.getType().name())
            .transactionDate(transaction.getTransactionDate())
            .notes(transaction.getNotes())
            .accountId(transaction.getAccount().getId())
            .categoryId(transaction.getCategory().getId())
            .createdAt(transaction.getCreatedAt())
            .updatedAt(transaction.getUpdatedAt())
            .build();
    }
}
