package br.com.ufape.spendfy.repository;

import br.com.ufape.spendfy.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByUsuarioId(Long idUsuario);

    boolean existsByNomeAndUsuarioId(String nome, Long idUsuario);
}
