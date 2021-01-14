package com.merlobranco.springboot.app.view.pdf;

import java.awt.Color;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.lowagie.text.Document;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.merlobranco.springboot.app.models.entity.Factura;
import com.merlobranco.springboot.app.models.entity.ItemFactura;

@Component("factura/ver")
public class FacturaPdfView extends AbstractPdfView {
	
	@Autowired
    private MessageSource messageSource;
	
	@Autowired
    private LocaleResolver localeResolver;

	@Override
	protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Locale locale = localeResolver.resolveLocale(request);
		
		// Other alternative of retrieving the translated content
		// Manage the locale inside so we could translate the texts directly
		MessageSourceAccessor mensajes =  getMessageSourceAccessor();
		
		Factura factura = (Factura)model.get("factura");
		
		// Table with customer data
		PdfPTable tCliente = new PdfPTable(1);
		tCliente.setSpacingAfter(20);
		
		// Customer title
		PdfPCell cell = new PdfPCell(new Phrase(messageSource.getMessage("text.factura.ver.datos.cliente", null, locale)));
		cell.setBackgroundColor(new Color(184, 218, 255));
		cell.setPadding(8f);
		
		tCliente.addCell(cell);
		tCliente.addCell(factura.getCliente().getNombre() + " "+ factura.getCliente().getApellido());
		tCliente.addCell(factura.getCliente().getEmail());
		
		// Table with invoice data
		PdfPTable tFactura = new PdfPTable(1);
		tFactura.setSpacingAfter(20);
		
		// Invoice title
		cell = new PdfPCell(new Phrase(messageSource.getMessage("text.factura.ver.datos.factura", null, locale)));
		cell.setBackgroundColor(new Color(195, 230, 203));
		cell.setPadding(8f);
		
		tFactura.addCell(cell);
		tFactura.addCell(mensajes.getMessage("text.cliente.factura.folio") + ": " +  factura.getId());
		tFactura.addCell(mensajes.getMessage("text.cliente.factura.descripcion") + ": " + factura.getDescripcion());
		tFactura.addCell(mensajes.getMessage("text.cliente.factura.fecha") + ": " + factura.getCreateAt());
		
		// Table with invoice lines data
		PdfPTable tDetalleFactura = new PdfPTable(4);
		tDetalleFactura.setWidths(new float [] {3.5f, 1, 1, 1});
		tDetalleFactura.addCell(mensajes.getMessage("text.factura.form.item.nombre"));
		tDetalleFactura.addCell(mensajes.getMessage("text.factura.form.item.precio"));
		tDetalleFactura.addCell(mensajes.getMessage("text.factura.form.item.cantidad"));
		tDetalleFactura.addCell(mensajes.getMessage("text.factura.form.item.total"));
		
		for (ItemFactura item : factura.getItems()) {
			tDetalleFactura.addCell(item.getProducto().getNombre());
			tDetalleFactura.addCell(item.getProducto().getPrecio().toString());
			
			// Amount
			cell = new PdfPCell(new Phrase(item.getCantidad().toString()));
			cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			
			tDetalleFactura.addCell(cell);
			tDetalleFactura.addCell(item.calcularImporte().toString());
		}
		
		cell = new PdfPCell(new Phrase(mensajes.getMessage("text.factura.form.total") + ": "));
		cell.setColspan(3);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		tDetalleFactura.addCell(cell);
		tDetalleFactura.addCell(factura.getTotal().toString());
		
		document.add(tCliente);
		document.add(tFactura);
		document.add(tDetalleFactura);
		
	}

}
