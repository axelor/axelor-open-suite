package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.businessproject.service.extracharges.ExtraChargeConstants;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExtrachargeType;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.timesheet.TimesheetInvoiceServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.axelor.i18n.I18n;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimesheetProjectInvoiceServiceImpl extends TimesheetInvoiceServiceImpl {

  private static final Logger log =
      LoggerFactory.getLogger(TimesheetProjectInvoiceServiceImpl.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM");

  protected TimesheetProjectService timesheetProjectService;
  protected ProductRepository productRepository;

  @Inject
  public TimesheetProjectInvoiceServiceImpl(
      AppHumanResourceService appHumanResourceService,
      PartnerPriceListService partnerPriceListService,
      ProductCompanyService productCompanyService,
      PriceListService priceListService,
      UnitConversionService unitConversionService,
      UnitConversionForProjectService unitConversionForProjectService,
      TimesheetProjectService timesheetProjectService,
      TimesheetLineService timesheetLineService,
      ProductRepository productRepository) {
    super(
        appHumanResourceService,
        partnerPriceListService,
        productCompanyService,
        priceListService,
        unitConversionService,
        unitConversionForProjectService,
        timesheetLineService);
    this.timesheetProjectService = timesheetProjectService;
    this.productRepository = productRepository;
  }

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<TimesheetLine> timesheetLineList, int priority) throws AxelorException {

    if (!appHumanResourceService.isApp("business-project")) {
      return super.createInvoiceLines(invoice, timesheetLineList, priority);
    }

    boolean consolidate = appHumanResourceService.getAppTimesheet().getConsolidateTSLine();

    // Process timesheet lines and calculate extra charges
    TimesheetAggregator aggregator = new TimesheetAggregator(consolidate);
    processTimesheetLines(timesheetLineList, aggregator);

    // Generate invoice lines
    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;

    count = createRegularInvoiceLines(invoice, aggregator, invoiceLineList, priority, count);
    count = createExtraChargeInvoiceLines(invoice, aggregator, invoiceLineList, priority, count);

    return invoiceLineList;
  }

  /** Process all timesheet lines to aggregate data and calculate extra charges */
  private void processTimesheetLines(
      List<TimesheetLine> timesheetLineList, TimesheetAggregator aggregator) {

    for (TimesheetLine timesheetLine : timesheetLineList) {
      Product product = getProductForLine(timesheetLine);
      Employee employee = timesheetLine.getEmployee();
      Project project = timesheetLine.getProject();

      BigDecimal totalHours = calculateTotalHours(timesheetLine);
      BigDecimal rate = product.getSalePrice();

      // Parse extra charge breakdown
      Map<String, Object> extraChargeBreakdown = parseExtraChargeBreakdown(timesheetLine);

      // Process extra charges
      if (extraChargeBreakdown != null && !extraChargeBreakdown.isEmpty()) {
        processExtraCharges(timesheetLine, extraChargeBreakdown, rate, aggregator);
      }

      // Process emergency extra charge
      if (Boolean.TRUE.equals(timesheetLine.getIsEmergencyService())) {
        aggregator.addEmergencySource(timesheetLine);
        aggregator.addEmergencyAmount(totalHours.multiply(rate));
      }

      // Aggregate timesheet data
      aggregator.addTimesheetData(timesheetLine, product, employee, project, totalHours);
    }
  }

  /** Process extra charges from extra charge breakdown */
  private void processExtraCharges(
      TimesheetLine timesheetLine,
      Map<String, Object> extraChargeBreakdown,
      BigDecimal rate,
      TimesheetAggregator aggregator) {

    for (ExtrachargeType type : ExtrachargeType.values()) {
      if (extraChargeBreakdown.containsKey(type.name())) {
        BigDecimal hours = getExtraChargeDuration(extraChargeBreakdown, type.name());

        if (hours.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal amount = normalise(hours).multiply(rate);
          aggregator.addExtraCharge(type, timesheetLine, amount);
        }
      }
    }
  }

  /** Create regular invoice lines from aggregated timesheet data */
  private int createRegularInvoiceLines(
      Invoice invoice,
      TimesheetAggregator aggregator,
      List<InvoiceLine> invoiceLineList,
      int priority,
      int count)
      throws AxelorException {

    for (Map.Entry<String, TimesheetData> entry : aggregator.getTimesheetDataMap().entrySet()) {
      String key = entry.getKey();
      TimesheetData data = entry.getValue();

      String dateStr = formatDateRange(data, aggregator.isConsolidate());
      PriceList priceList = data.project.getPriceList();

      List<InvoiceLine> lines =
          createInvoiceLine(
              invoice,
              data.product,
              data.project,
              data.employee,
              dateStr,
              data.hours,
              priority * 100 + count,
              priceList,
              data.forcedUnitPrice,
              data.forcedPriceDiscounted);

      invoiceLineList.addAll(lines);
      InvoiceLine createdLine = invoiceLineList.get(invoiceLineList.size() - 1);
      createdLine.setProject(data.project);
      createdLine.setSourceType(ExtraChargeConstants.TIMESHEET_INVOICE_LINE_SOURCE_TYPE);

      // Set source IDs
      List<Long> sourceIds = aggregator.getTimesheetSourceIds(key);
      if (sourceIds != null && !sourceIds.isEmpty()) {
        createdLine.setSourceTimesheetLineIds(sourceIdsToString(sourceIds));
      }

      count++;
    }

    return count;
  }

  /** Create extra charge invoice lines */
  private int createExtraChargeInvoiceLines(
      Invoice invoice,
      TimesheetAggregator aggregator,
      List<InvoiceLine> invoiceLineList,
      int priority,
      int count)
      throws AxelorException {

    Project firstProject = aggregator.getFirstProject();
    if (firstProject == null) {
      return count;
    }

    // Calculate date range from all timesheet lines
    String dateRange = aggregator.getDateRange();

    // Process each extra charge type
    for (ExtrachargeType type : ExtrachargeType.values()) {
      BigDecimal amount = aggregator.getExtraChargeAmount(type);
      if (amount.compareTo(BigDecimal.ZERO) > 0) {
        InvoiceLine extraChargeLine =
            createExtraChargeInvoiceLine(
                invoice,
                type,
                amount,
                priority * 100 + count,
                firstProject,
                aggregator.getExtraChargeSources(type),
                dateRange);

        if (extraChargeLine != null) {
          invoiceLineList.add(extraChargeLine);
          count++;
        }
      }
    }

    // Emergency extra charge
    BigDecimal emergencyAmount = aggregator.getEmergencyAmount();
    if (emergencyAmount.compareTo(BigDecimal.ZERO) > 0) {
      InvoiceLine emergencyLine =
          createEmergencyExtraChargeInvoiceLine(
              invoice,
              emergencyAmount,
              priority * 100 + count,
              firstProject,
              aggregator.getEmergencySources(),
              dateRange);

      if (emergencyLine != null) {
        invoiceLineList.add(emergencyLine);
        count++;
      }
    }

    return count;
  }

  /** Create an extra charge invoice line for a specific extra charge type */
  private InvoiceLine createExtraChargeInvoiceLine(
      Invoice invoice,
      ExtrachargeType type,
      BigDecimal baseAmount,
      int priority,
      Project project,
      List<TimesheetLine> sourceTimesheetLines,
      String dateRange)
      throws AxelorException {

    String productCode = ExtraChargeConstants.getExtraChargeProductCode(type);
    Product extraChargeProduct = productRepository.findByCode(productCode);

    if (extraChargeProduct == null) {
      log.warn("No product found for code {}", productCode);
      return null;
    }

    // Calculate total duration for this extra charge type
    BigDecimal totalDuration = calculateExtraChargeDuration(type, sourceTimesheetLines);
    log.debug("ExtraCharge {} has total duration: {} hours", type.name(), totalDuration);

    return createExtraChargeInvoiceLineCommon(
        invoice,
        extraChargeProduct,
        baseAmount,
        totalDuration,
        priority,
        project,
        sourceTimesheetLines,
        ExtraChargeConstants.EXTRACHARGE_INVOICE_LINE_SOURCE_TYPE,
        dateRange);
  }

  /** Create emergency extra charge invoice line */
  private InvoiceLine createEmergencyExtraChargeInvoiceLine(
      Invoice invoice,
      BigDecimal baseAmount,
      int priority,
      Project project,
      List<TimesheetLine> sourceTimesheetLines,
      String dateRange)
      throws AxelorException {

    Product extraChargeProduct =
        productRepository.findByCode(ExtraChargeConstants.EMERGENCY_PRODUCT_CODE);

    if (extraChargeProduct == null) {
      log.warn("No product found for code {}", ExtraChargeConstants.EMERGENCY_PRODUCT_CODE);
      return null;
    }

    // Calculate total emergency hours
    BigDecimal totalDuration =
        sourceTimesheetLines.stream()
            .map(this::calculateTotalHours)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    log.debug("Emergency extra charge has total duration: {} hours", totalDuration);

    return createExtraChargeInvoiceLineCommon(
        invoice,
        extraChargeProduct,
        baseAmount,
        totalDuration,
        priority,
        project,
        sourceTimesheetLines,
        ExtraChargeConstants.EXTRACHARGE_INVOICE_LINE_SOURCE_TYPE,
        dateRange);
  }

  /** Common logic for creating extra charge invoice lines */
  private InvoiceLine createExtraChargeInvoiceLineCommon(
      Invoice invoice,
      Product extraChargeProduct,
      BigDecimal baseAmount,
      BigDecimal duration,
      int priority,
      Project project,
      List<TimesheetLine> sourceTimesheetLines,
      String sourceType,
      String dateRange)
      throws AxelorException {

    BigDecimal extraChargePercentage = extraChargeProduct.getSalePrice();
    if (extraChargePercentage == null) {
      extraChargePercentage = BigDecimal.ZERO;
    }

    BigDecimal decimalPercentage =
        extraChargePercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

    BigDecimal extraChargeAmount =
        baseAmount.multiply(decimalPercentage).setScale(2, RoundingMode.HALF_UP);

    // Get product name using company context
    String productName =
        (String) productCompanyService.get(extraChargeProduct, "name", invoice.getCompany());

    productName =
        dateRange != null && !dateRange.isEmpty()
            ? productName + " " + "(" + dateRange + ")"
            : productName;

    Unit unit = extraChargeProduct.getUnit();

    // Add duration to description
    String description = "";
    if (duration.compareTo(BigDecimal.ZERO) > 0) {
      description = String.format(I18n.get("Billed for %.2f Hour(s)"), duration);
    }

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            extraChargeProduct,
            productName,
            normalise(baseAmount),
            extraChargeAmount,
            extraChargeAmount,
            description,
            decimalPercentage,
            unit,
            null,
            priority,
            BigDecimal.ZERO,
            PriceListLineRepository.AMOUNT_TYPE_NONE,
            extraChargeAmount,
            extraChargeAmount,
            false) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {
            InvoiceLine invoiceLine = this.createInvoiceLine();
            invoiceLine.setProject(project);
            invoiceLine.setSourceType(sourceType);

            if (sourceTimesheetLines != null && !sourceTimesheetLines.isEmpty()) {
              invoiceLine.setSourceTimesheetLineIds(
                  sourceTimesheetLines.stream()
                      .map(tsl -> String.valueOf(tsl.getId()))
                      .collect(Collectors.joining(",")));
            }

            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);
            return invoiceLines;
          }
        };

    List<InvoiceLine> lines = invoiceLineGenerator.creates();
    return lines.isEmpty() ? null : lines.get(0);
  }

  /* Helper Methods */

  private Product getProductForLine(TimesheetLine timesheetLine) {
    Product product = timesheetLine.getProduct();
    if (product == null) {
      product = timesheetLineService.getDefaultProduct(timesheetLine);
    }
    return product;
  }

  private BigDecimal calculateTotalHours(TimesheetLine timesheetLine) {
    try {
      return timesheetLine.getDurationForCustomer() != null
          ? timesheetProjectService.computeDurationForCustomer(timesheetLine)
          : timesheetLine.getHoursDuration();
    } catch (AxelorException e) {
      log.error(
          "Error calculating total hours for timesheet line {}: {}",
          timesheetLine.getId(),
          e.getMessage());
      // Fallback to hoursDuration if computation fails
      return timesheetLine.getHoursDuration() != null
          ? timesheetLine.getHoursDuration()
          : BigDecimal.ZERO;
    }
  }

  /** Calculate the total duration for a specific extra charge type from source timesheet lines */
  private BigDecimal calculateExtraChargeDuration(
      ExtrachargeType type, List<TimesheetLine> sourceTimesheetLines) {

    BigDecimal totalDuration = BigDecimal.ZERO;

    for (TimesheetLine line : sourceTimesheetLines) {
      Map<String, Object> extraChargeBreakdown = parseExtraChargeBreakdown(line);
      if (extraChargeBreakdown != null && extraChargeBreakdown.containsKey(type.name())) {
        BigDecimal duration = getExtraChargeDuration(extraChargeBreakdown, type.name());
        totalDuration = totalDuration.add(duration);
      }
    }

    return totalDuration;
  }

  private Map<String, Object> parseExtraChargeBreakdown(TimesheetLine timesheetLine) {
    try {
      if (timesheetLine.getExtraChargeBreakdown() != null
          && !timesheetLine.getExtraChargeBreakdown().isEmpty()) {
        return OBJECT_MAPPER.readValue(
            timesheetLine.getExtraChargeBreakdown(), new TypeReference<Map<String, Object>>() {});
      }
    } catch (JsonProcessingException e) {
      log.error("Error parsing extra charge breakdown JSON: {}", e.getMessage());
    }
    return null;
  }

  private BigDecimal getExtraChargeDuration(Map<String, Object> extraChargeBreakdown, String key) {
    Object duration = extraChargeBreakdown.get(key);
    if (duration == null) {
      return BigDecimal.ZERO;
    }
    if (duration instanceof Number) {
      return BigDecimal.valueOf(((Number) duration).doubleValue())
          .setScale(2, RoundingMode.HALF_UP);
    }
    try {
      return new BigDecimal(duration.toString());
    } catch (NumberFormatException e) {
      return BigDecimal.ZERO;
    }
  }

  private String formatDateRange(TimesheetData data, boolean consolidate) {
    if (consolidate && data.startDate != null && data.endDate != null) {
      return data.startDate.format(DATE_FORMAT) + " - " + data.endDate.format(DATE_FORMAT);
    }
    return data.startDate != null ? data.startDate.format(DATE_FORMAT) : "";
  }

  private String sourceIdsToString(List<Long> sourceIds) {
    return sourceIds.stream().map(String::valueOf).collect(Collectors.joining(","));
  }

  private BigDecimal normalise(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
  }

  // Inner classes

  /** Aggregates timesheet data and extra charge information */
  private static final class TimesheetAggregator {
    private final boolean consolidate;
    private final Map<String, TimesheetData> timesheetDataMap = new HashMap<>();
    private final Map<String, List<Long>> timesheetSourceIdsMap = new HashMap<>();

    private final Map<ExtrachargeType, List<TimesheetLine>> extraChargeSources =
        new EnumMap<>(ExtrachargeType.class);
    private final Map<ExtrachargeType, BigDecimal> extraChargeAmounts =
        new EnumMap<>(ExtrachargeType.class);

    private final List<TimesheetLine> emergencySources = new ArrayList<>();
    private BigDecimal emergencyAmount = BigDecimal.ZERO;

    private TimesheetAggregator(boolean consolidate) {
      this.consolidate = consolidate;
      // Initialize maps
      for (ExtrachargeType type : ExtrachargeType.values()) {
        extraChargeSources.put(type, new ArrayList<>());
        extraChargeAmounts.put(type, BigDecimal.ZERO);
      }
    }

    public void addTimesheetData(
        TimesheetLine line, Product product, Employee employee, Project project, BigDecimal hours) {

      ProjectTask projectTask = line.getProjectTask();
      BigDecimal forcedUnitPrice = null;
      BigDecimal forcedPriceDiscounted = null;

      // This is commented out as it forces an activity (product) on a timesheet line based on the
      // projectTask (selectiing and activity on the project task so it preffils when creating a new
      // timesheet line.
      // which was introduced by one of our modifications)
      // and this causes the product which gets invoiced to be different from that selected in the
      // timesheet line if the prefilled timesheet line activity (product) is changed or even if
      // it's not changed,
      // this introduces a forced unit price on the selected product causing the unit price of the
      // invoiced product not being that
      // defined in the product's record.

      //      if (projectTask != null && projectTask.getProduct() != null) {
      //        product = projectTask.getProduct();
      //        forcedUnitPrice = projectTask.getUnitPrice();
      //        forcedPriceDiscounted = projectTask.getPriceDiscounted();
      //      }

      String key = generateKey(product, employee, project, line, consolidate);

      if (timesheetDataMap.containsKey(key)) {
        TimesheetData existing = timesheetDataMap.get(key);
        existing.updateDates(line.getStartTime().toLocalDate(), line.getDate());
        existing.addHours(hours);
        timesheetSourceIdsMap.get(key).add(line.getId());
      } else {
        TimesheetData data =
            new TimesheetData(
                product,
                employee,
                line.getStartTime().toLocalDate(),
                line.getEndTime().toLocalDate(),
                hours,
                project,
                forcedUnitPrice,
                forcedPriceDiscounted);
        timesheetDataMap.put(key, data);

        List<Long> sourceIds = new ArrayList<>();
        sourceIds.add(line.getId());
        timesheetSourceIdsMap.put(key, sourceIds);
      }
    }

    public void addExtraCharge(ExtrachargeType type, TimesheetLine line, BigDecimal amount) {
      List<TimesheetLine> sources = extraChargeSources.get(type);
      if (!sources.contains(line)) {
        sources.add(line);
      }
      extraChargeAmounts.put(type, extraChargeAmounts.get(type).add(amount));
    }

    public void addEmergencySource(TimesheetLine line) {
      if (!emergencySources.contains(line)) {
        emergencySources.add(line);
      }
    }

    public void addEmergencyAmount(BigDecimal amount) {
      emergencyAmount = emergencyAmount.add(amount);
    }

    private String generateKey(
        Product product,
        Employee employee,
        Project project,
        TimesheetLine line,
        boolean consolidate) {
      if (consolidate) {
        return (product != null ? product.getId() + "|" : "")
            + employee.getId()
            + "|"
            + project.getId();
      } else {
        return String.valueOf(line.getId());
      }
    }

    public Map<String, TimesheetData> getTimesheetDataMap() {
      return timesheetDataMap;
    }

    public List<Long> getTimesheetSourceIds(String key) {
      return timesheetSourceIdsMap.get(key);
    }

    public BigDecimal getExtraChargeAmount(ExtrachargeType type) {
      return extraChargeAmounts.get(type);
    }

    public List<TimesheetLine> getExtraChargeSources(ExtrachargeType type) {
      return extraChargeSources.get(type);
    }

    public BigDecimal getEmergencyAmount() {
      return emergencyAmount;
    }

    public List<TimesheetLine> getEmergencySources() {
      return emergencySources;
    }

    public Project getFirstProject() {
      return timesheetDataMap.isEmpty()
          ? null
          : timesheetDataMap.values().iterator().next().project;
    }

    public boolean isConsolidate() {
      return consolidate;
    }

    public String getDateRange() {
      if (timesheetDataMap.isEmpty()) {
        return "";
      }

      LocalDate earliestStart = null;
      LocalDate latestEnd = null;

      for (TimesheetData data : timesheetDataMap.values()) {
        if (earliestStart == null || data.startDate.isBefore(earliestStart)) {
          earliestStart = data.startDate;
        }
        if (latestEnd == null || data.endDate.isAfter(latestEnd)) {
          latestEnd = data.endDate;
        }
      }

      if (consolidate && earliestStart != null && latestEnd != null) {
        return earliestStart.format(DATE_FORMAT) + " - " + latestEnd.format(DATE_FORMAT);
      } else if (earliestStart != null) {
        return earliestStart.format(DATE_FORMAT);
      }
      return "";
    }
  }

  /** Data holder for timesheet information */
  private static final class TimesheetData {
    Product product;
    Employee employee;
    LocalDate startDate;
    LocalDate endDate;
    BigDecimal hours;
    Project project;
    BigDecimal forcedUnitPrice;
    BigDecimal forcedPriceDiscounted;

    private TimesheetData(
        Product product,
        Employee employee,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal hours,
        Project project,
        BigDecimal forcedUnitPrice,
        BigDecimal forcedPriceDiscounted) {
      this.product = product;
      this.employee = employee;
      this.startDate = startDate;
      this.endDate = endDate;
      this.hours = hours;
      this.project = project;
      this.forcedUnitPrice = forcedUnitPrice;
      this.forcedPriceDiscounted = forcedPriceDiscounted;
    }

    public void updateDates(LocalDate newStart, LocalDate newEnd) {
      if (newStart.compareTo(startDate) < 0) {
        startDate = newStart;
      }
      if (newEnd.compareTo(endDate) > 0) {
        endDate = newEnd;
      }
    }

    public void addHours(BigDecimal additionalHours) {
      hours = hours.add(additionalHours);
    }
  }
}
