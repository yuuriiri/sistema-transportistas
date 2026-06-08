package cl.duoc.transportista.services;

import cl.duoc.transportista.models.Guia;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generarPdf(Guia guia) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("Guía de Despacho", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph(" "));

            // Tabla con datos
            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);

            agregarFila(tabla, "N° Guía", guia.getNumeroGuia());
            agregarFila(tabla, "Transportista", guia.getTransportista());
            agregarFila(tabla, "Fecha Despacho", guia.getFechaDespacho());
            agregarFila(tabla, "Estado", guia.getEstado());
            agregarFila(tabla, "Creado En", guia.getCreadoEn().toString());

            document.add(tabla);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }

        return out.toByteArray();
    }

    private void agregarFila(PdfPTable tabla, String campo, String valor) {
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        PdfPCell celdaCampo = new PdfPCell(new Phrase(campo, boldFont));
        celdaCampo.setPadding(8);
        tabla.addCell(celdaCampo);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, normalFont));
        celdaValor.setPadding(8);
        tabla.addCell(celdaValor);
    }
}