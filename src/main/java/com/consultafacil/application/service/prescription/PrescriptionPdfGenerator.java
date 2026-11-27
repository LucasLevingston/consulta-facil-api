package com.consultafacil.application.service.prescription;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class PrescriptionPdfGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generate(String professionalName, String licenseNumber, String specialty,
                           String patientName, String medications, String instructions,
                           LocalDate validUntil, LocalDate issuedDate) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 70, 70);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(100, 100, 100));

            // Letterhead
            Paragraph header = new Paragraph();
            header.add(new Phrase(professionalName + "\n", titleFont));
            header.add(new Phrase(specialty + " | " + licenseNumber + "\n", smallFont));
            header.add(new Phrase("Consulta Fácil — Plataforma de Saúde Digital\n", smallFont));
            header.setSpacingAfter(20);
            document.add(header);

            // Title
            Paragraph title = new Paragraph("RECEITA MÉDICA DIGITAL", headerFont);
            title.setAlignment(1);
            title.setSpacingAfter(16);
            document.add(title);

            // Patient info
            Paragraph patientSection = new Paragraph();
            patientSection.add(new Phrase("Paciente: ", headerFont));
            patientSection.add(new Phrase(patientName + "\n", normalFont));
            patientSection.add(new Phrase("Data de emissão: ", headerFont));
            patientSection.add(new Phrase(issuedDate.format(DATE_FMT), normalFont));
            if (validUntil != null) {
                patientSection.add(new Phrase("    Válido até: ", headerFont));
                patientSection.add(new Phrase(validUntil.format(DATE_FMT), normalFont));
            }
            patientSection.setSpacingAfter(16);
            document.add(patientSection);

            // Medications
            Paragraph medTitle = new Paragraph("Medicamentos prescritos:", headerFont);
            medTitle.setSpacingAfter(8);
            document.add(medTitle);

            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            PdfPCell cell = new PdfPCell(new Phrase(medications, normalFont));
            cell.setPadding(8);
            cell.setBorderColor(new Color(200, 200, 200));
            table.addCell(cell);
            table.setSpacingAfter(16);
            document.add(table);

            // Instructions
            if (instructions != null && !instructions.isBlank()) {
                Paragraph instrTitle = new Paragraph("Orientações:", headerFont);
                instrTitle.setSpacingAfter(6);
                document.add(instrTitle);
                Paragraph instrBody = new Paragraph(instructions, normalFont);
                instrBody.setSpacingAfter(24);
                document.add(instrBody);
            }

            // Signature line
            Paragraph sig = new Paragraph("___________________________________\n" + professionalName + "\n" + licenseNumber, normalFont);
            sig.setAlignment(2);
            document.add(sig);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Falha ao gerar PDF de receita: {}", e.getMessage(), e);
            throw new IllegalStateException("Falha ao gerar PDF de receita", e);
        }
    }
}
