package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.insight.InsightMensalResponse;
import br.com.ufape.spendfy.dto.insight.PrevisaoGastoResponse;
import br.com.ufape.spendfy.dto.insight.ScoreResponse;
import br.com.ufape.spendfy.service.InsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
@Tag(name = "Insights", description = "Previsões, score financeiro e análise com IA")
@SecurityRequirement(name = "bearerAuth")
public class InsightController {

    private final InsightService insightService;

    @GetMapping("/previsao")
    @Operation(summary = "Previsão de gastos mensais",
               description = "Projeta o gasto de cada categoria com base na média ponderada dos últimos 3 meses")
    public ResponseEntity<List<PrevisaoGastoResponse>> getPrevisao() {
        return ResponseEntity.ok(insightService.getPrevisaoGastos());
    }

    @GetMapping("/score")
    @Operation(summary = "Score financeiro",
               description = "Calcula um score de 0 a 100 com base em aderência a orçamentos, relação receita/despesa e tendência de saldo")
    public ResponseEntity<ScoreResponse> getScore() {
        return ResponseEntity.ok(insightService.calcularScore());
    }

    @GetMapping("/relatorio-mensal")
    @Operation(summary = "Relatório mensal com IA",
               description = "Gera um resumo personalizado das finanças do mês usando inteligência artificial")
    public ResponseEntity<InsightMensalResponse> getInsightMensal() {
        return ResponseEntity.ok(insightService.getInsightMensal());
    }
}
