package com.axelor.apps.businessproject.service.extracharges;

import com.axelor.apps.account.db.Invoice;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.helpers.file.PdfHelper;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceBreakdownPrintServiceImpl implements InvoiceBreakdownPrintService {

  private static final Logger log = LoggerFactory.getLogger(InvoiceBreakdownPrintServiceImpl.class);

  @Override
  public String printInvoiceBreakdown(Invoice invoice) throws Exception {
    log.debug("Generating PDF breakdown for invoice {}", invoice.getId());
    List<Map<String, Object>> displayData =
        Beans.get(InvoiceBreakdownDisplayService.class).generateBreakdownFromInvoice(invoice);

    String bodyHtml = buildHtmlFromData(displayData);
    String fullHtml = wrapHtml(bodyHtml, invoice);

    byte[] pdfBytes = convertHtmlToPdf(fullHtml);

    String fileName = getFileName(invoice);
    log.debug("Filename {}", fileName);

    MetaFiles metaFiles = Beans.get(MetaFiles.class);

    // Delete all existing invoice breakdown attachments
    List<DMSFile> existingBreakdowns = findExistingAttachments(invoice, fileName);
    for (DMSFile existing : existingBreakdowns) {
      metaFiles.delete(existing);
    }

    // create temp file to write breakdown content
    Path tempFile = MetaFiles.createTempFile(null, ".pdf");
    try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
      fos.write(pdfBytes);
    }

    MetaFile metaFile = metaFiles.upload(new FileInputStream(tempFile.toFile()), fileName);
    metaFiles.attach(metaFile, fileName, invoice);

    return PdfHelper.getFileLinkFromPdfFile(tempFile.toFile(), fileName);
  }

  @Override
  public String buildHtmlFromData(List<Map<String, Object>> data) {

    StringBuilder html = new StringBuilder();
    html.append("<table style='width: 100%; border-collapse: collapse;'>");
    html.append("<thead><tr>");
    html.append("<th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>#</th>");
    html.append("<th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>")
        .append(I18n.get("Description"))
        .append("</th>");
    html.append("<th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>")
        .append(I18n.get("Quantity"))
        .append("</th>");
    html.append("<th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>")
        .append(I18n.get("Unit"))
        .append("</th>");
    html.append("<th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>")
        .append(I18n.get("Price"))
        .append("</th>");
    html.append("<th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>")
        .append(I18n.get("Amount"))
        .append("</th>");
    html.append("<th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>")
        .append(I18n.get("Billing Details"))
        .append("</th>");
    html.append("</tr></thead><tbody>");

    for (Map<String, Object> line : data) {
      String sectionStyle = (String) line.get("sectionStyle");

      if ("SPACING".equals(sectionStyle)) {
        html.append("<tr style='height: 15px;'>");
        html.append("<td colspan='7' style='border: none;'>&nbsp;</td>");
        html.append("</tr>");
        continue;
      }

      String style = "";
      if (Boolean.TRUE.equals(line.get("isBold"))) {
        style = "font-weight: bold;";
      }

      html.append("<tr style='").append(style).append("'>");
      html.append("<td style='padding: 10px; border: 1px solid #ddd;'>")
          .append(line.get("sequence") != null ? line.get("sequence") : "")
          .append("</td>");
      html.append("<td style='padding: 10px; border: 1px solid #ddd;'>")
          .append(line.get("description"))
          .append("</td>");
      html.append("<td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>")
          .append(formatValue(line.get("quantity")))
          .append("</td>");
      html.append("<td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>")
          .append(line.get("unit") != null ? line.get("unit") : "")
          .append("</td>");
      html.append("<td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>")
          .append(formatValue(line.get("price")))
          .append("</td>");
      html.append("<td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>")
          .append(formatValue(line.get("amount")))
          .append("</td>");
      html.append("<td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>")
          .append(formatValue(line.get("billingDetails") != null ? line.get("billingDetails") : ""))
          .append("</td>");
      html.append("</tr>");
    }

    html.append("</tbody></table>");
    return html.toString();
  }

  private String wrapHtml(String bodyHtml, Invoice invoice) {

    StringBuilder html = new StringBuilder();

    html.append("<!DOCTYPE html><html><head>");
    html.append("<meta charset='UTF-8'/>");
    html.append("<style>");
    html.append("body{font-family:Arial;font-size:10pt;}");
    html.append("table{width:100%;border-collapse:collapse;}");
    html.append("th,td{border:1px solid #ddd;padding:8px;}");
    html.append("th{background:#f5f5f5;}");
    html.append("</style></head><body>");

    html.append("<h2>").append(I18n.get("Invoice Breakdown")).append("</h2>");
    html.append("<p><strong>")
        .append(I18n.get("Invoice"))
        .append(":</strong> ")
        .append(invoice.getInvoiceId())
        .append("<br/><strong>")
        .append(I18n.get("Date"))
        .append(":</strong> ")
        .append(invoice.getInvoiceDate())
        .append("<br/><strong>")
        .append(I18n.get("Customer"))
        .append(":</strong> ")
        .append(invoice.getPartner().getName())
        .append("</p>");

    html.append(bodyHtml);
    html.append("</body></html>");

    return html.toString();
  }

  /** Formats numeric values to 2 decimal places, returns empty string for null */
  private String formatValue(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof BigDecimal) {
      BigDecimal bd = (BigDecimal) value;
      return String.format("%.2f", bd);
    }
    return value.toString();
  }

  private byte[] convertHtmlToPdf(String html) throws Exception {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      ConverterProperties props = new ConverterProperties();
      props.setCharset("UTF-8");
      HtmlConverter.convertToPdf(html, os, props);
      return os.toByteArray();
    }
  }

  private List<DMSFile> findExistingAttachments(Invoice invoice, String fileName) {
    return Beans.get(DMSFileRepository.class)
        .all()
        .filter(
            "self.relatedId = ?1 AND self.relatedModel = ?2 AND self.fileName = ?3",
            invoice.getId(),
            Invoice.class.getName(),
            fileName)
        .fetch();
  }

  private String getFileName(Invoice invoice) {
    return String.format(
        "invoice-breakdown-%s.pdf",
        invoice.getInvoiceId() != null ? invoice.getInvoiceId() : invoice.getId());
  }
}
