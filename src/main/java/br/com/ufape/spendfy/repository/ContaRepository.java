package br.com.ufape.spendfy.repository;

import br.com.ufape.spendfy.entity.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {

    List<Conta> findByUsuarioId(Long idUsuario);

    boolean existsByNomeAndUsuarioId(String nome, Long idUsuario);
}
