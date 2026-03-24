package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.component.AuthenticatedUserResolver;
import br.com.ufape.spendfy.dto.alerta.AlertaResponse;
import br.com.ufape.spendfy.entity.Alerta;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.enums.TipoAlerta;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.repository.AlertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final AuthenticatedUserResolver userResolver;

    @Transactional(readOnly = true)
    public List<AlertaResponse> listarNaoLidos() {
        Usuario usuario = userResolver.getUsuarioAutenticado();
        return alertaRepository.findByUsuarioIdAndLidoFalseOrderByCriadoEmDesc(usuario.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlertaResponse marcarComoLido(Long id) {
        Usuario usuario = userResolver.getUsuarioAutenticado();
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", "id", id));

        if (!alerta.getUsuario().getId().equals(usuario.getId())) {
            throw new br.com.ufape.spendfy.exception.BusinessException("Alerta não pertence ao usuário");
        }

        alerta.setLido(true);
        alertaRepository.save(alerta);
        return toResponse(alerta);
    }

    @Transactional
    public void criarAlerta(Usuario usuario, TipoAlerta tipo, String mensagem, Long idReferencia) {
        LocalDateTime depois = LocalDateTime.now().minusHours(24);
        boolean jaExiste = alertaRepository.existsByUsuarioIdAndTipoAndIdReferenciaAndCriadoEmAfter(
                usuario.getId(), tipo, idReferencia, depois);

        if (jaExiste) return;

        Alerta alerta = Alerta.builder()
                .tipo(tipo)
                .mensagem(mensagem)
                .usuario(usuario)
                .idReferencia(idReferencia)
                .build();

        alertaRepository.save(alerta);
    }

    private AlertaResponse toResponse(Alerta alerta) {
        return AlertaResponse.builder()
                .id(alerta.getId())
                .tipo(alerta.getTipo())
                .mensagem(alerta.getMensagem())
                .lido(alerta.isLido())
                .idReferencia(alerta.getIdReferencia())
                .criadoEm(alerta.getCriadoEm())
                .build();
    }
}
