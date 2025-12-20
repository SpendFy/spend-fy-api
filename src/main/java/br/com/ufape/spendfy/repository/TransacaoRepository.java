package br.com.ufape.spendfy.repository;

import br.com.ufape.spendfy.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByUsuarioId(Long idUsuario);

    List<Transacao> findByUsuarioIdAndContaId(Long idUsuario, Long idConta);

    List<Transacao> findByUsuarioIdAndCategoriaId(Long idUsuario, Long idCategoria);

    List<Transacao> findByUsuarioIdAndDataBetween(Long idUsuario, LocalDate dataInicio, LocalDate dataFim);

    List<Transacao> findByUsuarioIdAndTipo(Long idUsuario, String tipo);
}
