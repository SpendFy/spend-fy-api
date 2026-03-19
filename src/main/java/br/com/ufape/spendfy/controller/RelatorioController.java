package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> baixarRelatorio() {
        byte[] pdf = relatorioService.gerarRelatorioPdf();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-spendFy.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    
    @GetMapping("/csv")
    public ResponseEntity<byte[]> baixarRelatorioCsv() {
        String csv = relatorioService.gerarRelatorioCsv();
        byte[] dados = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-spendfy.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(dados);
    }
}