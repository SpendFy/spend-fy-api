package br.com.ufape.spendfy.repository;

import br.com.ufape.spendfy.entity.Alerta;
import br.com.ufape.spendfy.enums.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByUsuarioIdAndLidoFalseOrderByCriadoEmDesc(Long idUsuario);

    boolean existsByUsuarioIdAndTipoAndIdReferenciaAndCriadoEmAfter(
            Long idUsuario, TipoAlerta tipo, Long idReferencia, LocalDateTime depois);
}
