package com.axelor.apps.businessproject.service.extracharges;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.i18n.I18n;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceBreakdownDisplayServiceImpl implements InvoiceBreakdownDisplayService {

  private static final Logger log =
      LoggerFactory.getLogger(InvoiceBreakdownDisplayServiceImpl.class);

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

    // Classify invoice lines
    InvoiceLineClassification classification = classifyInvoiceLines(invoice.getInvoiceLineList());

    // Process sections
    sequence = processTimesheetSection(displayLines, classification, sequence);
    sequence = processNightShiftSection(displayLines, classification, sequence);
    sequence = processExpenseSection(displayLines, classification.expenseLines, sequence);
    sequence = processOtherSection(displayLines, classification.otherLines, sequence);

    addTotalsAndVAT(displayLines, invoice, sequence);

    return displayLines;
  }

  /** Classify invoice lines into categories */
  private InvoiceLineClassification classifyInvoiceLines(List<InvoiceLine> invoiceLines) {
    return new InvoiceLineClassification(
        invoiceLineClassifier.getTimesheetLines(invoiceLines),
        invoiceLineClassifier.getExpenseLines(invoiceLines),
        invoiceLineClassifier.getExtraChargeLines(invoiceLines),
        invoiceLineClassifier.getOtherLines(invoiceLines));
  }

  /** Process timesheet section with base hours and extra charges */
  private int processTimesheetSection(
      List<Map<String, Object>> displayLines,
      InvoiceLineClassification classification,
      int sequence) {

    if (classification.timesheetLines.isEmpty()) {
      return sequence;
    }

    // Add base timesheet lines
    BigDecimal baseTotal = BigDecimal.ZERO;
    for (InvoiceLine line : classification.timesheetLines) {
      displayLines.add(createTimesheetDisplayLine(sequence++, line));
      baseTotal = baseTotal.add(getAmountOrZero(line.getExTaxTotal()));
    }

    displayLines.add(createTotalLine(I18n.get("Base Total"), baseTotal));

    // Add extra charges in order
    BigDecimal extraChargeTotal = baseTotal;
    for (String productCode : ExtraChargeConstants.EXTRA_CHARGE_CODES_EXCEPT_NIGHT) {
      InvoiceLine extraCharge = findExtraChargeByCode(classification.extraChargeLines, productCode);
      if (extraCharge != null) {
        displayLines.add(createExtraChargeLine(sequence++, extraCharge, false));
        extraChargeTotal = extraChargeTotal.add(getAmountOrZero(extraCharge.getExTaxTotal()));
      }
    }

    if (extraChargeTotal.compareTo(baseTotal) > 0) {
      displayLines.add(createTotalLine(I18n.get("Extra Charge Total"), extraChargeTotal));
    }

    return sequence;
  }

  /** Process night shift section with breakdown */
  private int processNightShiftSection(
      List<Map<String, Object>> displayLines,
      InvoiceLineClassification classification,
      int sequence) {

    Optional<InvoiceLine> nightExtraChargeOpt =
        classification.extraChargeLines.stream()
            .filter(
                line ->
                    line.getProductCode() != null
                        && line.getProductCode()
                            .toUpperCase(Locale.ROOT)
                            .contains(ExtraChargeConstants.NIGHTSHIFT_PRODUCT_CODE))
            .findFirst();

    if (nightExtraChargeOpt.isEmpty()) {
      return sequence;
    }

    InvoiceLine nightExtraCharge = nightExtraChargeOpt.get();
    displayLines.add(createSpacingLine());

    // Get night hours breakdown
    List<Long> timesheetIds = parseTimesheetIds(nightExtraCharge.getSourceTimesheetLineIds());
    Map<String, NightHoursService.NightHoursDetail> nightBreakdown =
        nightHoursService.getNightHoursBreakdown(timesheetIds);

    // Add night breakdown lines
    int nightSeq = 1;
    BigDecimal nightTotal = BigDecimal.ZERO;
    for (Map.Entry<String, NightHoursService.NightHoursDetail> entry : nightBreakdown.entrySet()) {
      NightHoursService.NightHoursDetail detail = entry.getValue();
      displayLines.add(createNightBreakdownLine(nightSeq++, entry.getKey(), detail));
      nightTotal = nightTotal.add(detail.getAmount());
    }

    displayLines.add(createTotalLine(I18n.get("Night Shift Total"), nightTotal));
    displayLines.add(createExtraChargeLine(sequence, nightExtraCharge, true));
    displayLines.add(createSpacingLine());

    return sequence;
  }

  /** Process expense lines section */
  private int processExpenseSection(
      List<Map<String, Object>> displayLines, List<InvoiceLine> expenseLines, int sequence) {
    return processGenericSection(displayLines, expenseLines, sequence, I18n.get("Expense Total"));
  }

  /** Process other lines section */
  private int processOtherSection(
      List<Map<String, Object>> displayLines, List<InvoiceLine> otherLines, int sequence) {
    return processGenericSection(displayLines, otherLines, sequence, "--");
  }

  /** Generic section processor for simple invoice lines */
  private int processGenericSection(
      List<Map<String, Object>> displayLines,
      List<InvoiceLine> lines,
      int sequence,
      String defaultUnit) {

    for (InvoiceLine line : lines) {
      displayLines.add(
          createDisplayLine(
              sequence++,
              line.getProductName() != null ? line.getProductName() : I18n.get("Other Item"),
              line.getQty(),
              line.getUnit() != null ? line.getUnit().getName() : defaultUnit,
              line.getPrice(),
              line.getExTaxTotal(),
              false,
              null,
              line.getDescription()));
    }
    return sequence;
  }

  /** Add invoice totals and VAT */
  private void addTotalsAndVAT(
      List<Map<String, Object>> displayLines, Invoice invoice, int sequence) {

    // Total without tax
    if (invoice.getExTaxTotal() != null) {
      displayLines.add(createTotalLine(I18n.get("Total W.T"), invoice.getExTaxTotal()));
    }

    // VAT
    if (invoice.getTaxTotal() != null && invoice.getTaxTotal().compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal vatPercent = calculateVatPercentage(invoice);
      displayLines.add(createVATLine(sequence, vatPercent, invoice.getTaxTotal(), invoice));
    }

    // Total with tax
    displayLines.add(createTotalLine(I18n.get("Total Amount"), invoice.getInTaxTotal()));
  }

  // Display Line Creators

  private Map<String, Object> createTimesheetDisplayLine(int sequence, InvoiceLine line) {
    return createDisplayLine(
        sequence,
        line.getProductName(),
        line.getQty(),
        line.getUnit() != null ? line.getUnit().getName() : I18n.get("Hour"),
        line.getPrice(),
        line.getExTaxTotal(),
        false,
        null,
        line.getDescription());
  }

  private Map<String, Object> createNightBreakdownLine(
      int sequence, String description, NightHoursService.NightHoursDetail detail) {
    String billingDetails =
        String.format(I18n.get("Billed for %.2f hour(s)"), detail.getNightHours());
    return createDisplayLine(
        sequence,
        description,
        detail.getNightHours(),
        I18n.get("Hour(s)"),
        detail.getRate(),
        detail.getAmount(),
        false,
        null,
        billingDetails);
  }

  private Map<String, Object> createExtraChargeLine(
      int sequence, InvoiceLine extraChargeLine, Boolean isBold) {
    BigDecimal displayPercentage =
        getAmountOrZero(extraChargeLine.getQty())
            .multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP);

    // Use invoice line's product name
    return createDisplayLine(
        sequence,
        extraChargeLine.getProductName(),
        displayPercentage,
        "%",
        extraChargeLine.getPrice(),
        extraChargeLine.getExTaxTotal(),
        isBold,
        extraChargeLine.getSourceTimesheetLineIds(),
        extraChargeLine.getDescription());
  }

  private Map<String, Object> createTotalLine(String description, BigDecimal amount) {
    return createDisplayLine(null, description, null, "", null, amount, true, null, null);
  }

  private Map<String, Object> createVATLine(
      int sequence, BigDecimal vatPercent, BigDecimal amount, Invoice invoice) {
    String currency = "EUR"; // default
    if (invoice != null
        && invoice.getCurrency() != null
        && invoice.getCurrency().getCodeISO() != null) {
      currency = invoice.getCurrency().getCodeISO();
    }

    return createDisplayLine(
        sequence,
        I18n.get("VAT") + " (" + String.format("%.0f%%", vatPercent) + ")",
        BigDecimal.ONE,
        currency,
        null,
        amount,
        false,
        null,
        null);
  }

  private Map<String, Object> createSpacingLine() {
    return createDisplayLine(null, "", null, "", null, null, false, null, null);
  }

  /** Generic display line creator */
  private Map<String, Object> createDisplayLine(
      Integer sequence,
      String description,
      BigDecimal quantity,
      String unit,
      BigDecimal price,
      BigDecimal amount,
      boolean isBold,
      String sourceLineIds,
      String billingDetails) {

    Map<String, Object> line = new HashMap<>();
    line.put("sequence", sequence);
    line.put("description", description != null ? description : "");
    line.put("quantity", quantity);
    line.put("unit", unit != null ? unit : "");
    line.put("price", price);
    line.put("amount", amount);
    line.put("isBold", isBold);

    if (billingDetails == null && unit != null && price != null) {
      billingDetails = String.format(I18n.get("Billed for %.2f %s(s)"), quantity, unit);
    }
    line.put("billingDetails", billingDetails);

    if (sourceLineIds != null && !sourceLineIds.isEmpty()) {
      line.put("sourceDetails", getSourceTimesheetDetails(sourceLineIds));
    }

    return line;
  }

  // Helper Methods

  /** Find extra charge line by product code */
  private InvoiceLine findExtraChargeByCode(
      List<InvoiceLine> extraChargeLines, String productCode) {
    return extraChargeLines.stream()
        .filter(
            l ->
                l.getProductCode() != null
                    && l.getProductCode().toUpperCase(Locale.ROOT).contains(productCode))
        .findFirst()
        .orElse(null);
  }

  private List<Long> parseTimesheetIds(String timesheetLineIds) {
    if (timesheetLineIds == null || timesheetLineIds.isEmpty()) {
      return Collections.emptyList();
    }

    return Arrays.stream(timesheetLineIds.split(","))
        .filter(s -> !s.isEmpty())
        .map(String::trim)
        .map(Long::valueOf)
        .collect(Collectors.toList());
  }

  private BigDecimal getAmountOrZero(BigDecimal amount) {
    return amount != null ? amount : BigDecimal.ZERO;
  }

  private BigDecimal calculateVatPercentage(Invoice invoice) {
    if (invoice.getExTaxTotal() != null && invoice.getExTaxTotal().compareTo(BigDecimal.ZERO) > 0) {
      return invoice
          .getTaxTotal()
          .divide(invoice.getExTaxTotal(), 4, BigDecimal.ROUND_HALF_UP)
          .multiply(BigDecimal.valueOf(100));
    }
    return BigDecimal.valueOf(19);
  }

  private String getSourceTimesheetDetails(String timesheetLineIds) {
    if (timesheetLineIds == null || timesheetLineIds.isEmpty()) {
      return "";
    }

    try {
      List<Long> ids = parseTimesheetIds(timesheetLineIds);
      List<TimesheetLine> lines =
          timesheetLineRepo.all().filter("self.id IN (:ids)").bind("ids", ids).fetch();

      return lines.stream().map(this::formatTimesheetLineDetail).collect(Collectors.joining(", "));
    } catch (Exception e) {
      return I18n.get("Error loading source details:") + " " + e.getMessage();
    }
  }

  private String formatTimesheetLineDetail(TimesheetLine tsl) {
    return String.format(
        I18n.get("%.2f hours on %s by %s"),
        tsl.getHoursDuration(),
        tsl.getDate(),
        tsl.getEmployee() != null ? tsl.getEmployee().getName() : I18n.get("Unknown"));
  }

  /**
   * Calculate the actual duration for a extra charge from its source timesheet lines This extracts
   * the duration from the extraChargeBreakdown Map stored in timesheet lines
   */
  private BigDecimal calculateExtraChargeDurationFromSources(InvoiceLine extraChargeLine) {
    String sourceIds = extraChargeLine.getSourceTimesheetLineIds();
    if (sourceIds == null || sourceIds.isEmpty()) {
      return BigDecimal.ZERO;
    }

    try {
      List<Long> ids = parseTimesheetIds(sourceIds);
      List<TimesheetLine> lines =
          timesheetLineRepo.all().filter("self.id IN (:ids)").bind("ids", ids).fetch();

      // Determine which extra charge type this is based on product code
      String productCode = extraChargeLine.getProductCode();
      if (productCode == null) {
        return BigDecimal.ZERO;
      }

      String extraChargeKey = determineExtraChargeKey(productCode);
      if (extraChargeKey == null) {
        return BigDecimal.ZERO;
      }

      // Sum up durations from all source timesheet lines
      BigDecimal totalDuration = BigDecimal.ZERO;
      for (TimesheetLine tsl : lines) {
        BigDecimal lineDuration = extractDurationFromBreakdown(tsl, extraChargeKey);
        totalDuration = totalDuration.add(lineDuration);
      }

      return totalDuration;
    } catch (Exception e) {
      log.error(I18n.get("Error calculating extra charge duration: {}"), e.getMessage());
      return BigDecimal.ZERO;
    }
  }

  /** Determine the extra charge key in the extraChargeBreakdown JSON based on product code */
  private String determineExtraChargeKey(String productCode) {
    String code = productCode.toUpperCase(Locale.ROOT);
    if (code.contains(ExtraChargeConstants.SATURDAY_PRODUCT_CODE))
      return ExtraChargeConstants.SATURDAY_PRODUCT_CODE;
    if (code.contains(ExtraChargeConstants.SUNDAY_PRODUCT_CODE))
      return ExtraChargeConstants.SUNDAY_PRODUCT_CODE;
    if (code.contains(ExtraChargeConstants.HOLIDAY_PRODUCT_CODE))
      return ExtraChargeConstants.HOLIDAY_PRODUCT_CODE;
    if (code.contains(ExtraChargeConstants.NIGHTSHIFT_PRODUCT_CODE))
      return ExtraChargeConstants.NIGHTSHIFT_PRODUCT_CODE;
    // Emergency doesn't store duration in breakdown, uses full line duration
    return null;
  }

  /**
   * Extract duration for a specific extra charge type from timesheet line's extraChargeBreakdown
   */
  private BigDecimal extractDurationFromBreakdown(TimesheetLine tsl, String extraChargeKey) {
    String breakdown = tsl.getExtraChargeBreakdown();
    if (breakdown == null || breakdown.isEmpty()) {
      return BigDecimal.ZERO;
    }

    try {
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> breakdownMap = mapper.readValue(breakdown, HashMap.class);

      Object duration = breakdownMap.get(extraChargeKey);
      if (duration == null) {
        return BigDecimal.ZERO;
      }

      if (duration instanceof Number) {
        return BigDecimal.valueOf(((Number) duration).doubleValue());
      }

      return new BigDecimal(duration.toString());
    } catch (Exception e) {
      log.error(
          "Error parsing extra charge breakdown for timesheet line {}: {}",
          tsl.getId(),
          e.getMessage());
      return BigDecimal.ZERO;
    }
  }

  // Inner Classes

  /** Holder for classified invoice lines */
  private static class InvoiceLineClassification {
    final List<InvoiceLine> timesheetLines;
    final List<InvoiceLine> expenseLines;
    final List<InvoiceLine> extraChargeLines;
    final List<InvoiceLine> otherLines;

    InvoiceLineClassification(
        List<InvoiceLine> timesheetLines,
        List<InvoiceLine> expenseLines,
        List<InvoiceLine> extraChargeLines,
        List<InvoiceLine> otherLines) {
      this.timesheetLines = timesheetLines;
      this.expenseLines = expenseLines;
      this.extraChargeLines = extraChargeLines;
      this.otherLines = otherLines;
    }
  }
}
