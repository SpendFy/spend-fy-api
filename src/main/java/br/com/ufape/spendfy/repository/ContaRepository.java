package br.com.ufape.spendfy.repository;

import br.com.ufape.spendfy.entity.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContaRepository extends JpaRepository<Conta, String> {
    List<Conta> findByUserId(String userId);
    List<Conta> findByUserIdAndActiveTrue(String userId);
    Optional<Conta> findByIdAndUserId(String id, String userId);
    boolean existsByUserIdAndName(String userId, String name);
}