package com.axelor.apps.businessproject.service.extracharges;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.common.StringUtils;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceBreakdownPrintServiceImpl implements InvoiceBreakdownPrintService {

  private static final Logger log = LoggerFactory.getLogger(InvoiceBreakdownPrintServiceImpl.class);

  @Override
  public String printInvoiceBreakdown(Invoice invoice) throws Exception {
    log.debug("Generating PDF breakdown for invoice {}", invoice.getId());

    PrintingTemplate template =
        Beans.get(AccountConfigService.class)
            .getInvoiceBreakdownPrintTemplate(invoice.getCompany());
    List<Map<String, Object>> displayData =
        Beans.get(InvoiceBreakdownDisplayService.class).generateBreakdownFromInvoice(invoice);

    String bodyHtml = buildHtmlFromData(displayData);
    String fullHtml = wrapHtml(bodyHtml, invoice);

    byte[] pdfBytes = convertHtmlToPdf(fullHtml);

    String fileName = getFileName(invoice, template);
    log.debug("Filename {}", fileName);

    MetaFiles metaFiles = Beans.get(MetaFiles.class);

    // create temp file to write breakdown content
    Path tempFile = MetaFiles.createTempFile(null, ".pdf");
    try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
      fos.write(pdfBytes);
    }

    if (template.getToAttach()) {
      if (template.getOverrideAttachment()) {
        // Delete all existing invoice breakdown attachments
        List<DMSFile> existingBreakdowns = findExistingAttachments(invoice, fileName);
        for (DMSFile existing : existingBreakdowns) {
          metaFiles.delete(existing);
        }
      }
      MetaFile metaFile = metaFiles.upload(new FileInputStream(tempFile.toFile()), fileName);
      metaFiles.attach(metaFile, fileName, invoice);
    }

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

    LocalDate invoiceDate =
        invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : LocalDate.now();

    Company company = invoice.getCompany();

    String companyName = company != null ? company.getName() : "";
    String logoImg = "";

    if (company != null && company.getLogo() != null) {
      logoImg = metaFileToBase64Img(company.getLogo());
    }

    StringBuilder html = new StringBuilder();

    // HEADER (logo + company only)
    html.append("<table class='header'>");
    html.append("<tr>");

    // Logo
    html.append("<td style='width:30%;'>");
    if (!logoImg.isEmpty()) {
      html.append("<img src='").append(logoImg).append("' style='max-height:60px;'/>");
    }
    html.append("</td>");

    // Company name
    html.append("<td style='width:70%; text-align:right;'>");
    html.append("<div class='company-name'>").append(companyName).append("</div>");
    html.append("</td>");

    html.append("</tr>");
    html.append("</table>");

    // CENTERED TITLE
    html.append("<div style='font-weight: bold; margin: 20px 0;'>")
        .append(I18n.get("Invoice Breakdown"))
        .append("</div>");

    // META INFO
    html.append("<p>");
    html.append("<strong>")
        .append(I18n.get("Order Number"))
        .append(":</strong> ")
        .append(invoice.getProject().getCode())
        .append("<br/>");
    html.append("<strong>")
        .append(I18n.get("Creation Date"))
        .append(":</strong> ")
        .append(invoiceDate)
        .append("<br/>");
    html.append("<strong>")
        .append(I18n.get("Customer"))
        .append(":</strong> ")
        .append(invoice.getPartner().getName())
        .append("<br/>");
    html.append("<strong>")
        .append(I18n.get("Customer cost center"))
        .append(":</strong> ")
        .append(invoice.getPartner().getCustomerCostCenter());
    html.append("</p>");

    html.append(bodyHtml);
    html.append("</body></html>");

    return html.toString();
  }

  private String metaFileToBase64Img(MetaFile metaFile) {
    if (metaFile == null || metaFile.getFilePath() == null) {
      return "";
    }

    try {
      Path path = MetaFiles.getPath(metaFile);

      if (!Files.exists(path)) {
        log.warn("Logo file not found on disk: {}", path);
        return "";
      }

      try (FileInputStream fis = new FileInputStream(path.toFile())) {
        byte[] bytes = fis.readAllBytes();
        String base64 = Base64.getEncoder().encodeToString(bytes);

        String mime = metaFile.getFileType() != null ? metaFile.getFileType() : "image/png";

        return "data:" + mime + ";base64," + base64;
      }

    } catch (Exception e) {
      log.warn("Unable to load company logo", e);
      return "";
    }
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

  private String getFileName(Invoice invoice, PrintingTemplate template) throws AxelorException {
    log.debug(
        "Invoice Breakdown printing template: {}",
        Beans.get(AccountConfigService.class)
            .getInvoiceBreakdownPrintTemplate(invoice.getCompany()));

    String fileName =
        Beans.get(PrintingTemplatePrintService.class)
            .getPrintFileName(template, new PrintingGenFactoryContext(invoice));
    log.debug("Invoice Breakdown Template file name: {}", fileName);
    return fileName;
  }

  protected String getTemplateName(PrintingTemplate template) {
    if (StringUtils.notEmpty(template.getScriptFieldName())) {
      return template.getScriptFieldName();
    }
    return template.getName();
  }
}
