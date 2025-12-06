/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.timesheet.TimesheetInvoiceServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimesheetProjectInvoiceServiceImpl extends TimesheetInvoiceServiceImpl {

  private static final Logger log =
      LoggerFactory.getLogger(TimesheetProjectInvoiceServiceImpl.class);
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

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    DateTimeFormatter ddmmFormat = DateTimeFormatter.ofPattern("dd/MM");

    // Maps to accumulate data
    HashMap<String, Object[]> timesheetDataMap = new HashMap<>();
    HashMap<String, List<Long>> timesheetSourceIdsMap = new HashMap<>();

    // Track surcharge sources AND their amounts
    List<TimesheetLine> saturdaySources = new ArrayList<>();
    List<TimesheetLine> sundaySources = new ArrayList<>();
    List<TimesheetLine> holidaySources = new ArrayList<>();
    List<TimesheetLine> emergencySources = new ArrayList<>();
    List<TimesheetLine> nightShiftSources = new ArrayList<>();

    // Track amounts ONLY for lines that have the surcharge flags
    BigDecimal saturdayAmount = BigDecimal.ZERO;
    BigDecimal sundayAmount = BigDecimal.ZERO;
    BigDecimal holidayAmount = BigDecimal.ZERO;
    BigDecimal emergencyAmount = BigDecimal.ZERO;
    BigDecimal totalNightHoursAmount = BigDecimal.ZERO;

    boolean consolidate = appHumanResourceService.getAppTimesheet().getConsolidateTSLine();

    // FIRST PASS: Group timesheet lines and calculate surcharge bases
    for (TimesheetLine timesheetLine : timesheetLineList) {
      Product product = timesheetLine.getProduct();
      if (product == null) {
        product = timesheetLineService.getDefaultProduct(timesheetLine);
      }

      Employee employee = timesheetLine.getEmployee();
      Project project = timesheetLine.getProject();

      // Get total hours
      BigDecimal totalHours =
          timesheetLine.getDurationForCustomer() != null
              ? timesheetProjectService.computeDurationForCustomer(timesheetLine)
              : timesheetLine.getHoursDuration();

      // Get night hours
      BigDecimal nightHours =
          timesheetLine.getNightHours() != null ? timesheetLine.getNightHours() : BigDecimal.ZERO;

      // Calculate this line's amount
      BigDecimal rate = product.getSalePrice();
      BigDecimal lineAmount = totalHours.multiply(rate);

      // Track surcharge flags and amounts
      if (Boolean.TRUE.equals(timesheetLine.getIsSaturday())) {
        saturdaySources.add(timesheetLine);
        saturdayAmount = saturdayAmount.add(lineAmount);
      }
      if (Boolean.TRUE.equals(timesheetLine.getIsSunday())) {
        sundaySources.add(timesheetLine);
        sundayAmount = sundayAmount.add(lineAmount);
      }
      if (Boolean.TRUE.equals(timesheetLine.getIsPublicHoliday())) {
        holidaySources.add(timesheetLine);
        holidayAmount = holidayAmount.add(lineAmount);
      }
      if (Boolean.TRUE.equals(timesheetLine.getIsEmergencyService())) {
        emergencySources.add(timesheetLine);
        emergencyAmount = emergencyAmount.add(lineAmount);
      }
      if (nightHours.compareTo(BigDecimal.ZERO) > 0) {
        nightShiftSources.add(timesheetLine);
        BigDecimal nightAmount = nightHours.multiply(rate);
        totalNightHoursAmount = totalNightHoursAmount.add(nightAmount);
      }

      ProjectTask projectTask = timesheetLine.getProjectTask();
      BigDecimal forcedUnitPrice = null;
      BigDecimal forcedPriceDiscounted = null;
      if (projectTask != null && projectTask.getProduct() != null) {
        product = projectTask.getProduct();
        forcedUnitPrice = projectTask.getUnitPrice();
        forcedPriceDiscounted = projectTask.getPriceDiscounted();
      }

      // Prepare data array
      Object[] tabInformations = new Object[8];
      tabInformations[0] = product;
      tabInformations[1] = employee;
      tabInformations[2] = timesheetLine.getStartTime().toLocalDate();
      tabInformations[3] = timesheetLine.getEndTime().toLocalDate();
      tabInformations[4] = totalHours;
      tabInformations[5] = project;
      tabInformations[6] = forcedUnitPrice;
      tabInformations[7] = forcedPriceDiscounted;

      String key;
      if (consolidate) {
        key =
            (product != null ? product.getId() + "|" : "")
                + employee.getId()
                + "|"
                + project.getId();

        if (timesheetDataMap.containsKey(key)) {
          Object[] existing = timesheetDataMap.get(key);
          // Update dates
          if (timesheetLine.getStartTime().toLocalDate().compareTo((LocalDate) existing[2]) < 0) {
            existing[2] = timesheetLine.getDate();
          } else if (timesheetLine.getDate().compareTo((LocalDate) existing[3]) > 0) {
            existing[3] = timesheetLine.getDate();
          }
          // Add hours
          existing[4] = ((BigDecimal) existing[4]).add(totalHours);

          // Track source IDs
          timesheetSourceIdsMap.get(key).add(timesheetLine.getId());
        } else {
          timesheetDataMap.put(key, tabInformations);

          List<Long> sourceIds = new ArrayList<>();
          sourceIds.add(timesheetLine.getId());
          timesheetSourceIdsMap.put(key, sourceIds);
        }
      } else {
        key = String.valueOf(timesheetLine.getId());
        timesheetDataMap.put(key, tabInformations);

        List<Long> sourceIds = new ArrayList<>();
        sourceIds.add(timesheetLine.getId());
        timesheetSourceIdsMap.put(key, sourceIds);
      }
    }

    // SECOND PASS: Create invoice lines for regular hours
    for (Map.Entry<String, Object[]> entry : timesheetDataMap.entrySet()) {
      String key = entry.getKey();
      Object[] timesheetInfo = entry.getValue();

      Product product = (Product) timesheetInfo[0];
      Employee employee = (Employee) timesheetInfo[1];
      LocalDate startDate = (LocalDate) timesheetInfo[2];
      LocalDate endDate = (LocalDate) timesheetInfo[3];
      BigDecimal hours = (BigDecimal) timesheetInfo[4];
      Project project = (Project) timesheetInfo[5];
      BigDecimal forcedUnitPrice = (BigDecimal) timesheetInfo[6];
      BigDecimal forcedPriceDiscounted = (BigDecimal) timesheetInfo[7];

      String strDate =
          consolidate && startDate != null && endDate != null
              ? startDate.format(ddmmFormat) + " - " + endDate.format(ddmmFormat)
              : (startDate != null ? startDate.format(ddmmFormat) : "");

      PriceList priceList = project.getPriceList();

      List<InvoiceLine> lines =
          this.createInvoiceLine(
              invoice,
              product,
              project,
              employee,
              strDate,
              hours,
              priority * 100 + count,
              priceList,
              forcedUnitPrice,
              forcedPriceDiscounted);

      invoiceLineList.addAll(lines);
      InvoiceLine createdLine = invoiceLineList.get(invoiceLineList.size() - 1);
      createdLine.setProject(project);

      // Mark as timesheet source
      createdLine.setSourceType("TIMESHEET");
      List<Long> sourceIds = timesheetSourceIdsMap.get(key);
      if (sourceIds != null && !sourceIds.isEmpty()) {
        String idsString = sourceIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        createdLine.setSourceTimesheetLineIds(idsString);
      }

      count++;
    }

    // THIRD PASS: Add surcharge invoice lines
    Project firstProject =
        !timesheetDataMap.isEmpty()
            ? (Project) timesheetDataMap.values().iterator().next()[5]
            : null;

    if (firstProject != null) {
      // Saturday surcharge
      if (saturdayAmount.compareTo(BigDecimal.ZERO) > 0) {
        InvoiceLine saturdayLine =
            createSurchargeInvoiceLine(
                invoice,
                "SATURDAY",
                saturdayAmount,
                priority * 100 + count,
                firstProject,
                saturdaySources);
        if (saturdayLine != null) {
          invoiceLineList.add(saturdayLine);
          count++;
        }
      }

      // Sunday surcharge
      if (sundayAmount.compareTo(BigDecimal.ZERO) > 0) {
        InvoiceLine sundayLine =
            createSurchargeInvoiceLine(
                invoice,
                "SUNDAY",
                sundayAmount,
                priority * 100 + count,
                firstProject,
                sundaySources);
        if (sundayLine != null) {
          invoiceLineList.add(sundayLine);
          count++;
        }
      }

      // Holiday surcharge
      if (holidayAmount.compareTo(BigDecimal.ZERO) > 0) {
        InvoiceLine holidayLine =
            createSurchargeInvoiceLine(
                invoice,
                "HOLIDAY",
                holidayAmount,
                priority * 100 + count,
                firstProject,
                holidaySources);
        if (holidayLine != null) {
          invoiceLineList.add(holidayLine);
          count++;
        }
      }

      // Emergency surcharge
      if (emergencyAmount.compareTo(BigDecimal.ZERO) > 0) {
        InvoiceLine emergencyLine =
            createSurchargeInvoiceLine(
                invoice,
                "EMERGENCY",
                emergencyAmount,
                priority * 100 + count,
                firstProject,
                emergencySources);
        if (emergencyLine != null) {
          invoiceLineList.add(emergencyLine);
          count++;
        }
      }

      // Night shift surcharge
      if (totalNightHoursAmount.compareTo(BigDecimal.ZERO) > 0) {
        InvoiceLine nightSurchargeLine =
            createSurchargeInvoiceLine(
                invoice,
                "NIGHTSHIFT",
                totalNightHoursAmount,
                priority * 100 + count,
                firstProject,
                nightShiftSources);
        if (nightSurchargeLine != null) {
          invoiceLineList.add(nightSurchargeLine);
          count++;
        }
      }
    }

    return invoiceLineList;
  }

  /** Creates a extra charges invoice line */
  private InvoiceLine createSurchargeInvoiceLine(
      Invoice invoice,
      String productCode,
      BigDecimal baseAmount,
      int priority,
      Project project,
      List<TimesheetLine> sourceTimesheetLines)
      throws AxelorException {

    Product surchargeProduct = productRepository.findByCode(productCode);
    if (surchargeProduct == null) {
      log.warn("No product found for code {}", productCode);
      return null;
    }

    BigDecimal surchargePercentage = surchargeProduct.getSalePrice();
    if (surchargePercentage == null) {
      surchargePercentage = BigDecimal.ZERO;
    }

    // Convert to decimal
    BigDecimal decimalPercentage =
        surchargePercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

    // Calculate surcharge amount: 0.5 × 187.50 = 93.75
    BigDecimal surchargeAmount =
        baseAmount.multiply(decimalPercentage).setScale(2, RoundingMode.HALF_UP);

    String productName = surchargeProduct.getName();
    String description =
        String.format("%s: %.0f%% of %.2f", productName, surchargePercentage, baseAmount);

    Unit unit = surchargeProduct.getUnit(); // Get unit from product

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            surchargeProduct,
            productName,
            normalise(baseAmount),
            surchargeAmount,
            surchargeAmount,
            description,
            decimalPercentage,
            unit,
            null,
            priority,
            BigDecimal.ZERO,
            PriceListLineRepository.AMOUNT_TYPE_NONE,
            surchargeAmount,
            surchargeAmount,
            false) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {
            InvoiceLine invoiceLine = this.createInvoiceLine();
            invoiceLine.setProject(project);
            invoiceLine.setSourceType("SURCHARGE");

            if (sourceTimesheetLines != null && !sourceTimesheetLines.isEmpty()) {
              String ids =
                  sourceTimesheetLines.stream()
                      .map(tsl -> String.valueOf(tsl.getId()))
                      .collect(Collectors.joining(","));
              invoiceLine.setSourceTimesheetLineIds(ids);
            }

            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);
            return invoiceLines;
          }
        };

    List<InvoiceLine> lines = invoiceLineGenerator.creates();
    return lines.isEmpty() ? null : lines.get(0);
  }

  private BigDecimal normalise(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
  }
}
