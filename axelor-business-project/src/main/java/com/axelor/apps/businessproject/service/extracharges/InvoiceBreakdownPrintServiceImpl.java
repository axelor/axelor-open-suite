package com.axelor.apps.businessproject.service.extracharges;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.repo.TaskMemberReportRepository;
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
import java.time.format.DateTimeFormatter;
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

    String performancePeriod = getPerformancePeriod(invoice);

    StringBuilder html = new StringBuilder();

    html.append("<html>");
    html.append("<head>");
    html.append("<meta charset='UTF-8'/>");

    html.append("<style>");

    html.append("body { font-family: Arial, sans-serif; font-size: 14px; }");

    html.append(".page-header { position: running(header); }");
    html.append(
        ".page-footer { position: running(footer); font-size: 14px; margin-bottom: 8px; text-align: end; padding-top: 6px; }");

    html.append("@page {");
    html.append("  margin: 200px 40px 50px 40px;");
    html.append("  @top-left { content: element(header); }");
    html.append("  @bottom-center { content: element(footer); }");
    html.append("}");

    html.append(".pageNumber::before { content: counter(page); }");
    html.append(".totalPages::before { content: counter(pages); }");

    html.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
    html.append("th, td { border: 5px solid #ddd; padding: 6px; }");
    html.append("th { background-color: #f5f5f5; }");

    html.append(
        ".page-header div:first-child { font-size: 16px; font-weight: bold; margin-bottom: 10px; }");
    html.append(".page-header div:nth-child(2) { font-size: 16px; line-height: 1.6; }");

    html.append("</style>");
    html.append("</head>");
    html.append("<body>");

    // header
    html.append("<div class='page-header'>");

    html.append("<div style='font-size:20px; font-weight:bold; margin-bottom:6px;'>")
        .append(I18n.get("Invoice Breakdown"))
        .append("</div>");

    html.append("<div style='line-height:1.4;'>");
    html.append("<strong>")
        .append(I18n.get("Order Number"))
        .append(":</strong> ")
        .append(invoice.getProject().getCode())
        .append("<br/>");
    html.append("<strong>")
        .append(I18n.get("Order Title"))
        .append(":</strong> ")
        .append(invoice.getProject().getName())
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
        .append(invoice.getPartner().getCustomerCostCenter())
        .append("<br/>");
    html.append("<strong>")
        .append(I18n.get("Performance Period"))
        .append(":</strong> ")
        .append(performancePeriod);
    html.append("</div>");

    html.append("</div>");

    // footer
    html.append("<div class='page-footer'>");
    html.append(I18n.get("Page"))
        .append(" <span class='pageNumber'></span> / <span class='totalPages'></span>");
    html.append("</div>");

    // content
    html.append(bodyHtml);

    html.append("</body>");
    html.append("</html>");

    return html.toString();
  }

  /** get project performance period */
  private String getPerformancePeriod(Invoice invoice) {

    if (invoice == null || invoice.getProject() == null) {
      return "";
    }

    List<TaskMemberReport> reports =
        Beans.get(TaskMemberReportRepository.class)
            .all()
            .filter("self.taskReport.project = ?1", invoice.getProject())
            .fetch();

    if (reports.isEmpty()) {
      return "";
    }

    LocalDate minDate = null;
    LocalDate maxDate = null;

    for (TaskMemberReport report : reports) {

      if (report.getStartTime() != null) {
        LocalDate startDate = report.getStartTime().toLocalDate();
        if (minDate == null || startDate.isBefore(minDate)) {
          minDate = startDate;
        }
      }

      if (report.getEndTime() != null) {
        LocalDate endDate = report.getEndTime().toLocalDate();
        if (maxDate == null || endDate.isAfter(maxDate)) {
          maxDate = endDate;
        }
      }
    }

    if (minDate == null || maxDate == null) {
      return "";
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    if (minDate.equals(maxDate)) {
      return formatter.format(minDate);
    }

    return formatter.format(minDate) + " – " + formatter.format(maxDate);
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
