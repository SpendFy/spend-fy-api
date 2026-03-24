package br.com.ufape.spendfy.repository;

import br.com.ufape.spendfy.entity.Transacao;
import br.com.ufape.spendfy.entity.enums.RecorrenciaTransacao;
import br.com.ufape.spendfy.entity.enums.TipoTransacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long>, JpaSpecificationExecutor<Transacao> {

    List<Transacao> findByUsuarioId(Long idUsuario);

    Page<Transacao> findByUsuarioId(Long idUsuario, Pageable pageable);

    List<Transacao> findByUsuarioIdAndContaId(Long idUsuario, Long idConta);

    List<Transacao> findByUsuarioIdAndCategoriaId(Long idUsuario, Long idCategoria);

    List<Transacao> findByUsuarioIdAndDataBetween(Long idUsuario, LocalDate dataInicio, LocalDate dataFim);

    List<Transacao> findByUsuarioIdAndTipo(Long idUsuario, TipoTransacao tipo);

    List<Transacao> findByRecorrenciaNotAndDataProximaOcorrenciaLessThanEqual(
            RecorrenciaTransacao recorrencia, LocalDate data);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.conta.id = :contaId AND t.tipo = :tipo")
    BigDecimal sumValorByContaIdAndTipo(@Param("contaId") Long contaId, @Param("tipo") TipoTransacao tipo);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.categoria.id = :categoriaId AND t.usuario.id = :usuarioId AND t.data BETWEEN :dataInicio AND :dataFim AND t.tipo = :tipo")
    BigDecimal sumValorByCategoriaAndPeriodoAndTipo(@Param("categoriaId") Long categoriaId, @Param("usuarioId") Long usuarioId, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim, @Param("tipo") TipoTransacao tipo);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.usuario.id = :usuarioId AND t.tipo = :tipo AND t.data BETWEEN :dataInicio AND :dataFim")
    BigDecimal sumValorByUsuarioIdAndTipoAndPeriodo(@Param("usuarioId") Long usuarioId, @Param("tipo") TipoTransacao tipo, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.usuario.id = :usuarioId AND t.categoria.id = :categoriaId AND t.tipo = :tipo AND t.data BETWEEN :dataInicio AND :dataFim")
    BigDecimal sumValorByUsuarioIdAndCategoriaIdAndTipoAndPeriodo(@Param("usuarioId") Long usuarioId, @Param("categoriaId") Long categoriaId, @Param("tipo") TipoTransacao tipo, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT t.categoria.id, t.categoria.nome, t.categoria.cor, COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.usuario.id = :usuarioId AND t.tipo = :tipo AND t.data BETWEEN :dataInicio AND :dataFim GROUP BY t.categoria.id, t.categoria.nome, t.categoria.cor ORDER BY SUM(t.valor) DESC")
    List<Object[]> findGastosPorCategoria(@Param("usuarioId") Long usuarioId, @Param("tipo") TipoTransacao tipo, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}
