package br.com.ufape.spendfy.specification;

import br.com.ufape.spendfy.entity.Transacao;
import br.com.ufape.spendfy.enums.StatusTransacao;
import br.com.ufape.spendfy.enums.TipoTransacao;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class TransacaoSpecification {

    private TransacaoSpecification() {}

    public static Specification<Transacao> doUsuario(Long idUsuario) {
        return (root, query, cb) -> cb.equal(root.get("usuario").get("id"), idUsuario);
    }

    public static Specification<Transacao> comTipo(TipoTransacao tipo) {
        if (tipo == null) return null;
        return (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Transacao> comStatus(StatusTransacao status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Transacao> daCategoria(Long idCategoria) {
        if (idCategoria == null) return null;
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), idCategoria);
    }

    public static Specification<Transacao> daConta(Long idConta) {
        if (idConta == null) return null;
        return (root, query, cb) -> cb.equal(root.get("conta").get("id"), idConta);
    }

    public static Specification<Transacao> dataInicio(LocalDate dataInicio) {
        if (dataInicio == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("data"), dataInicio);
    }

    public static Specification<Transacao> dataFim(LocalDate dataFim) {
        if (dataFim == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("data"), dataFim);
    }
}
