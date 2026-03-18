package br.com.ufape.spendfy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import br.com.ufape.spendfy.entity.Transaction;
import br.com.ufape.spendfy.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);
    
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = :type ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserAndType(@Param("userId") Long userId, @Param("type") Transaction.TransactionType type);
}
