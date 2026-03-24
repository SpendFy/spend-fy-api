package br.com.ufape.spendfy.repository;

import br.com.ufape.spendfy.entity.Transacao;
import br.com.ufape.spendfy.entity.enums.TipoTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByUsuarioId(Long idUsuario);

    List<Transacao> findByUsuarioIdAndContaId(Long idUsuario, Long idConta);

    List<Transacao> findByUsuarioIdAndCategoriaId(Long idUsuario, Long idCategoria);

    List<Transacao> findByUsuarioIdAndDataBetween(Long idUsuario, LocalDate dataInicio, LocalDate dataFim);

    List<Transacao> findByUsuarioIdAndTipo(Long idUsuario, TipoTransacao tipo);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.conta.id = :contaId AND t.tipo = :tipo")
    BigDecimal sumValorByContaIdAndTipo(@Param("contaId") Long contaId, @Param("tipo") TipoTransacao tipo);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.categoria.id = :categoriaId AND t.usuario.id = :usuarioId AND t.data BETWEEN :dataInicio AND :dataFim AND t.tipo = :tipo")
    BigDecimal sumValorByCategoriaAndPeriodoAndTipo(@Param("categoriaId") Long categoriaId, @Param("usuarioId") Long usuarioId, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim, @Param("tipo") TipoTransacao tipo);
}
