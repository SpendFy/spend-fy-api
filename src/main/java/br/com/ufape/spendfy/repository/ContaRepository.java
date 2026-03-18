package br.com.ufape.spendfy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.ufape.spendfy.entity.Account;
import br.com.ufape.spendfy.entity.User;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    List<Account> findByUserIdOrderByCreatedAtDesc(Long userId);
}
