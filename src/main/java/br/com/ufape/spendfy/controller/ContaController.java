package br.com.ufape.spendfy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import br.com.ufape.spendfy.dto.AccountDTO;
import br.com.ufape.spendfy.service.AccountService;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody AccountDTO accountDTO, Authentication authentication) {
        AccountDTO created = accountService.createAccount(accountDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts(Authentication authentication) {
        List<AccountDTO> accounts = accountService.getAccountsByUser(authentication.getName());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Long id, Authentication authentication) {
        AccountDTO account = accountService.getAccountById(id, authentication.getName());
        return ResponseEntity.ok(account);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDTO> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountDTO accountDTO,
            Authentication authentication) {
        AccountDTO updated = accountService.updateAccount(id, accountDTO, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id, Authentication authentication) {
        accountService.deleteAccount(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
