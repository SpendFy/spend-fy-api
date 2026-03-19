package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.transacao.TransacaoResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final TransacaoService transacaoService;

    public byte[] gerarRelatorioPdf() {
        List<TransacaoResponse> transacoes = transacaoService.listarTodas();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, out);

        document.open();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Relatório Financeiro - SpendFy", fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10, 10, 25, 20, 20, 15});
        table.addCell("Data");
        table.addCell("Hora");
        table.addCell("Descrição");
        table.addCell("Categoria");
        table.addCell("Conta");
        table.addCell("Valor");

        for (TransacaoResponse t : transacoes) {
            table.addCell(t.getData().format(dateFormatter));
            
            String horaFormatada = (t.getDataCadastro() != null) 
                    ? t.getDataCadastro().format(timeFormatter) 
                    : "--:--:--";
            table.addCell(horaFormatada);
            table.addCell(t.getDescricao());
            table.addCell(t.getNomeCategoria());
            table.addCell(t.getNomeConta() != null ? t.getNomeConta() : "—");
            
            String valorPrefixo = t.getTipo().equals("RECEITA") ? "+ R$ " : "- R$ ";
            table.addCell(valorPrefixo + t.getValor());
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    public String gerarRelatorioCsv() {
        List<TransacaoResponse> transacoes = transacaoService.listarTodas();
        StringBuilder csv = new StringBuilder();

        csv.append("Data;Hora;Descrição;Categoria;Conta;Tipo;Valor\n");

        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        
        for (TransacaoResponse t : transacoes) {
            csv.append(t.getData().format(dateFormatter)).append(";");
            csv.append(t.getDataCadastro() != null ? t.getDataCadastro().format(timeFormatter) : "--:--:--").append(";");
            csv.append(t.getDescricao()).append(";");
            csv.append(t.getNomeCategoria()).append(";");
            csv.append(t.getNomeConta() != null ? t.getNomeConta() : "").append(";");
            csv.append(t.getTipo()).append(";");
            csv.append(t.getValor()).append("\n");
        }

        return csv.toString();
    }
}