package br.com.ufape.spendfy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.ufape.spendfy.entity.Category;
import br.com.ufape.spendfy.entity.User;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);
    List<Category> findByUserAndType(User user, Category.CategoryType type);
    List<Category> findByUserIdOrderByCreatedAtDesc(Long userId);
}
