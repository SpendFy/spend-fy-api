package br.com.ufape.spendfy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import br.com.ufape.spendfy.dto.TransactionDTO;
import br.com.ufape.spendfy.service.TransactionService;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO, Authentication authentication) {
        TransactionDTO created = transactionService.createTransaction(transactionDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(Authentication authentication) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByUser(authentication.getName());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id, Authentication authentication) {
        TransactionDTO transaction = transactionService.getTransactionById(id, authentication.getName());
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            Authentication authentication) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByDateRange(startDate, endDate, authentication.getName());
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDTO transactionDTO,
            Authentication authentication) {
        TransactionDTO updated = transactionService.updateTransaction(id, transactionDTO, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id, Authentication authentication) {
        transactionService.deleteTransaction(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
