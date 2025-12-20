package br.com.ufape.spendfy.repository;

import br.com.ufape.spendfy.entity.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    List<Orcamento> findByUsuarioId(Long idUsuario);

    List<Orcamento> findByUsuarioIdAndCategoriaId(Long idUsuario, Long idCategoria);

    @Query("SELECT o FROM Orcamento o WHERE o.usuario.id = :idUsuario " +
           "AND o.categoria.id = :idCategoria " +
           "AND ((o.dataInicio BETWEEN :dataInicio AND :dataFim) " +
           "OR (o.dataFim BETWEEN :dataInicio AND :dataFim) " +
           "OR (:dataInicio BETWEEN o.dataInicio AND o.dataFim) " +
           "OR (:dataFim BETWEEN o.dataInicio AND o.dataFim))")
    List<Orcamento> findOverlappingOrcamentos(
            @Param("idUsuario") Long idUsuario,
            @Param("idCategoria") Long idCategoria,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );
}
