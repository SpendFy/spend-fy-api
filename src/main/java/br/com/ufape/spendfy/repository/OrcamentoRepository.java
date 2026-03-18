package br.com.ufape.spendfy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.ufape.spendfy.entity.Budget;
import br.com.ufape.spendfy.entity.User;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser(User user);
    List<Budget> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Budget> findByUserAndPeriod(User user, Budget.BudgetPeriod period);
}
