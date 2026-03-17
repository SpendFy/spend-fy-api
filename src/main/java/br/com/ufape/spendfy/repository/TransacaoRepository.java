package br.com.ufape.spendfy.repository;

import br.com.ufape.spendfy.entity.Transacao;
import br.com.ufape.spendfy.entity.TipoTransacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, String> {
    Page<Transacao> findByUserId(String userId, Pageable pageable);
    List<Transacao> findByUserIdAndContaId(String userId, String contaId);
    Optional<Transacao> findByIdAndUserId(String id, String userId);
    
    @Query("SELECT t FROM Transacao t WHERE t.user.id = :userId AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transacao> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transacao t WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserAndTypeAndDateRange(String userId, TipoTransacao type, LocalDate startDate, LocalDate endDate);
}