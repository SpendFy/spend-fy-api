package br.com.ufape.spendfy.repository;

import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.CategoriaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, String> {
    List<Categoria> findByUserId(String userId);
    List<Categoria> findByUserIdAndType(String userId, CategoriaType type);
    Optional<Categoria> findByIdAndUserId(String id, String userId);
    boolean existsByUserIdAndName(String userId, String name);
}