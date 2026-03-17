package br.com.ufape.spendfy.repository;

import br.com.ufape.spendfy.entity.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, String> {
    List<Orcamento> findByUserId(String userId);
    Optional<Orcamento> findByIdAndUserId(String id, String userId);
    List<Orcamento> findByUserIdAndCategoriaId(String userId, String categoriaId);
    
    @Query("SELECT o FROM Orcamento o WHERE o.user.id = :userId AND o.categoria.id = :categoriaId AND " +
           "((o.startDate <= :date AND o.endDate >= :date) OR " +
           "(o.startDate <= :date2 AND o.endDate >= :date2))")
    List<Orcamento> findOverlappingBudgets(String userId, String categoriaId, LocalDate date, LocalDate date2);
}