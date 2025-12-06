package com.axelor.apps.businessproject.service.extracharges;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class InvoiceBreakdownDisplayServiceImpl implements InvoiceBreakdownDisplayService {

  @Inject private TimesheetLineRepository timesheetLineRepo;

  @Inject private InvoiceLineClassifier invoiceLineClassifier;

  @Inject private NightHoursService nightHoursService;

  @Override
  public List<Map<String, Object>> generateBreakdownFromInvoice(Invoice invoice) {
    if (invoice == null || invoice.getInvoiceLineList() == null) {
      return Collections.emptyList();
    }

    List<Map<String, Object>> displayLines = new ArrayList<>();
    int sequence = 1;

    List<InvoiceLine> timesheetLines =
        invoiceLineClassifier.getTimesheetLines(invoice.getInvoiceLineList());
    List<InvoiceLine> expenseLines =
        invoiceLineClassifier.getExpenseLines(invoice.getInvoiceLineList());
    List<InvoiceLine> surchargeLines =
        invoiceLineClassifier.getSurchargeLines(invoice.getInvoiceLineList());
    List<InvoiceLine> otherLines =
        invoiceLineClassifier.getOtherLines(invoice.getInvoiceLineList());

    sequence = processTimesheetSection(displayLines, timesheetLines, surchargeLines, sequence);
    sequence =
        processNightShiftSection(displayLines, surchargeLines, expenseLines, otherLines, sequence);
    sequence = processNormalSection(displayLines, expenseLines, "NORMAL", sequence, "Hour");
    sequence = processNormalSection(displayLines, otherLines, "NORMAL", sequence, "--");

    addTotalsAndVAT(displayLines, invoice, sequence);

    return displayLines;
  }

  private int processTimesheetSection(
      List<Map<String, Object>> displayLines,
      List<InvoiceLine> timesheetLines,
      List<InvoiceLine> surchargeLines,
      int sequence) {
    if (timesheetLines.isEmpty()) return sequence;

    BigDecimal total = BigDecimal.ZERO;
    for (InvoiceLine line : timesheetLines) {
      displayLines.add(
          createDisplayLine(
              sequence++,
              line.getProductName(),
              line.getQty(),
              line.getUnit() != null ? line.getUnit().getName() : "hours",
              line.getPrice(),
              line.getExTaxTotal(),
              false,
              null));
      total = total.add(Optional.ofNullable(line.getExTaxTotal()).orElse(BigDecimal.ZERO));
    }

    displayLines.add(createDisplayLine(null, "Base Total", null, "", null, total, true, null));

    for (String type : Arrays.asList("SATURDAY", "SUNDAY", "HOLIDAY", "EMERGENCY")) {
      for (InvoiceLine surcharge : surchargeLines) {
        if (surcharge.getProductName() != null
            && surcharge.getProductName().toUpperCase().contains(type)) {
          displayLines.add(createSurchargeLine(sequence++, surcharge));
          total = total.add(surcharge.getExTaxTotal());
          break; // only first match
        }
      }
    }

    if (total.compareTo(BigDecimal.ZERO) > 0) {
      displayLines.add(
          createDisplayLine(null, "Extra Charges Total", null, "", null, total, true, null));
    }

    return sequence;
  }

  private int processNightShiftSection(
      List<Map<String, Object>> displayLines,
      List<InvoiceLine> surchargeLines,
      List<InvoiceLine> expenseLines,
      List<InvoiceLine> otherLines,
      int sequence) {
    surchargeLines.stream()
        .filter(
            l -> l.getProductName() != null && l.getProductName().toUpperCase().contains("NIGHT"))
        .findFirst()
        .ifPresent(
            nightSurcharge -> {
              displayLines.add(createSpacingLine());

              List<Long> timesheetIds =
                  Arrays.stream(
                          Optional.ofNullable(nightSurcharge.getSourceTimesheetLineIds())
                              .orElse("")
                              .split(","))
                      .filter(s -> !s.isEmpty())
                      .map(Long::valueOf)
                      .collect(Collectors.toList());

              Map<String, NightHoursService.NightHoursDetail> nightBreakdown =
                  nightHoursService.getNightHoursBreakdown(timesheetIds);

              int nightSeq = 1;
              BigDecimal nightTotal = BigDecimal.ZERO;
              for (Map.Entry<String, NightHoursService.NightHoursDetail> entry :
                  nightBreakdown.entrySet()) {
                NightHoursService.NightHoursDetail detail = entry.getValue();
                displayLines.add(
                    createDisplayLine(
                        nightSeq++,
                        entry.getKey(),
                        detail.getNightHours(),
                        "hours",
                        detail.getRate(),
                        detail.getAmount(),
                        false,
                        null));
                nightTotal = nightTotal.add(detail.getAmount());
              }

              displayLines.add(
                  createDisplayLine(
                      null, "Night Shift Total", null, "", null, nightTotal, true, null));
              displayLines.add(createSurchargeLine(sequence, nightSurcharge));
              displayLines.add(createSpacingLine());
            });

    return sequence;
  }

  private int processNormalSection(
      List<Map<String, Object>> displayLines,
      List<InvoiceLine> lines,
      String sectionStyle,
      int sequence,
      String defaultUnit) {
    for (InvoiceLine line : lines) {
      displayLines.add(
          createDisplayLine(
              sequence++,
              line.getProductName() != null ? line.getProductName() : "Other Item",
              line.getQty(),
              line.getUnit() != null ? line.getUnit().getName() : defaultUnit,
              line.getPrice(),
              line.getExTaxTotal(),
              false,
              null));
    }
    return sequence;
  }

  private void addTotalsAndVAT(
      List<Map<String, Object>> displayLines, Invoice invoice, int sequence) {
    if (invoice.getExTaxTotal() != null) {
      displayLines.add(
          createDisplayLine(
              null, "Total W.T", null, "", null, invoice.getExTaxTotal(), true, null));
    }

    if (invoice.getTaxTotal() != null && invoice.getTaxTotal().compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal vatPercent =
          invoice.getExTaxTotal() != null && invoice.getExTaxTotal().compareTo(BigDecimal.ZERO) > 0
              ? invoice
                  .getTaxTotal()
                  .divide(invoice.getExTaxTotal(), 4, BigDecimal.ROUND_HALF_UP)
                  .multiply(BigDecimal.valueOf(100))
              : BigDecimal.valueOf(19);
      displayLines.add(
          createDisplayLine(
              sequence,
              "VAT (" + String.format("%.0f%%", vatPercent) + ")",
              BigDecimal.ONE,
              "Hours",
              null,
              invoice.getTaxTotal(),
              false,
              null));
    }

    displayLines.add(
        createDisplayLine(
            null, "Total Amount", null, "", null, invoice.getInTaxTotal(), true, null));
  }

  private Map<String, Object> createDisplayLine(
      Integer sequence,
      String description,
      BigDecimal quantity,
      String unit,
      BigDecimal price,
      BigDecimal amount,
      boolean isBold,
      String sourceLineIds) {
    Map<String, Object> line = new HashMap<>();
    line.put("sequence", sequence);
    line.put("description", description != null ? description : "");
    line.put("quantity", quantity);
    line.put("unit", unit != null ? unit : "");
    line.put("price", price);
    line.put("amount", amount);
    line.put("isBold", isBold);

    if (sourceLineIds != null && !sourceLineIds.isEmpty()) {
      line.put("sourceDetails", getSourceTimesheetDetails(sourceLineIds));
    }

    return line;
  }

  private Map<String, Object> createSurchargeLine(int sequence, InvoiceLine surchargeLine) {
    BigDecimal displayPercentage =
        Optional.ofNullable(surchargeLine.getQty())
            .orElse(BigDecimal.ZERO)
            .multiply(BigDecimal.valueOf(100))
            .setScale(0, BigDecimal.ROUND_HALF_UP);

    return createDisplayLine(
        sequence,
        surchargeLine.getProductName(),
        displayPercentage,
        "%",
        surchargeLine.getPrice(),
        surchargeLine.getExTaxTotal(),
        false,
        surchargeLine.getSourceTimesheetLineIds());
  }

  private Map<String, Object> createSpacingLine() {
    return createDisplayLine(null, "", null, "", null, null, false, null);
  }

  private String getSourceTimesheetDetails(String timesheetLineIds) {
    if (timesheetLineIds == null || timesheetLineIds.isEmpty()) return "";

    try {
      List<Long> ids =
          Arrays.stream(timesheetLineIds.split(","))
              .map(String::trim)
              .map(Long::valueOf)
              .collect(Collectors.toList());

      List<TimesheetLine> lines =
          timesheetLineRepo.all().filter("self.id IN (:ids)").bind("ids", ids).fetch();

      return lines.stream()
          .map(
              tsl ->
                  String.format(
                      "%.2f hours on %s by %s",
                      tsl.getHoursDuration(),
                      tsl.getDate(),
                      tsl.getEmployee() != null ? tsl.getEmployee().getName() : "Unknown"))
          .collect(Collectors.joining(", "));
    } catch (Exception e) {
      return "Error loading source details: " + e.getMessage();
    }
  }
}
