package br.com.ufape.spendfy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.ufape.spendfy.dto.AccountDTO;
import br.com.ufape.spendfy.entity.Account;
import br.com.ufape.spendfy.entity.User;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.repository.AccountRepository;
import br.com.ufape.spendfy.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public AccountDTO createAccount(AccountDTO accountDTO, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = Account.builder()
            .name(accountDTO.getName())
            .type(accountDTO.getType())
            .balance(accountDTO.getBalance())
            .description(accountDTO.getDescription())
            .user(user)
            .build();

        Account saved = accountRepository.save(account);
        return mapToDTO(saved);
    }

    public List<AccountDTO> getAccountsByUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return accountRepository.findByUser(user)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public AccountDTO getAccountById(Long id, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Unauthorized access to account");
        }

        return mapToDTO(account);
    }

    public AccountDTO updateAccount(Long id, AccountDTO accountDTO, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Unauthorized access to account");
        }

        account.setName(accountDTO.getName());
        account.setType(accountDTO.getType());
        account.setBalance(accountDTO.getBalance());
        account.setDescription(accountDTO.getDescription());

        Account updated = accountRepository.save(account);
        return mapToDTO(updated);
    }

    public void deleteAccount(Long id, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Unauthorized access to account");
        }

        accountRepository.delete(account);
    }

    private AccountDTO mapToDTO(Account account) {
        return AccountDTO.builder()
            .id(account.getId())
            .name(account.getName())
            .type(account.getType())
            .balance(account.getBalance())
            .description(account.getDescription())
            .createdAt(account.getCreatedAt())
            .updatedAt(account.getUpdatedAt())
            .build();
    }
}
