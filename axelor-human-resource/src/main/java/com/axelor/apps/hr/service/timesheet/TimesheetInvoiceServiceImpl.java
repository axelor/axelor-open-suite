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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TimesheetInvoiceServiceImpl implements TimesheetInvoiceService {

  protected AppHumanResourceService appHumanResourceService;
  protected PartnerPriceListService partnerPriceListService;
  protected ProductCompanyService productCompanyService;
  protected PriceListService priceListService;
  protected UnitConversionService unitConversionService;
  protected UnitConversionForProjectService unitConversionForProjectService;
  protected TimesheetLineService timesheetLineService;

  @Inject
  public TimesheetInvoiceServiceImpl(
      AppHumanResourceService appHumanResourceService,
      PartnerPriceListService partnerPriceListService,
      ProductCompanyService productCompanyService,
      PriceListService priceListService,
      UnitConversionService unitConversionService,
      UnitConversionForProjectService unitConversionForProjectService,
      TimesheetLineService timesheetLineService) {
    this.appHumanResourceService = appHumanResourceService;
    this.partnerPriceListService = partnerPriceListService;
    this.productCompanyService = productCompanyService;
    this.priceListService = priceListService;
    this.unitConversionService = unitConversionService;
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.timesheetLineService = timesheetLineService;
  }

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<TimesheetLine> timesheetLineList, int priority) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    DateFormat ddmmFormat = new SimpleDateFormat("dd/MM");
    HashMap<String, Object[]> timeSheetInformationsMap = new HashMap<>();
    // Check if a consolidation by product and user must be done
    boolean consolidate = appHumanResourceService.getAppTimesheet().getConsolidateTSLine();

    for (TimesheetLine timesheetLine : timesheetLineList) {
      Object[] tabInformations = new Object[5];
      Product product = timesheetLine.getProduct();
      if (product == null) {
        product = timesheetLineService.getDefaultProduct(timesheetLine);
      }
      tabInformations[0] = product;
      tabInformations[1] = timesheetLine.getEmployee();
      // Start date
      tabInformations[2] = timesheetLine.getDate();
      // End date, useful only for consolidation
      tabInformations[3] = timesheetLine.getDate();
      tabInformations[4] = timesheetLine.getHoursDuration();

      String key = null;
      if (consolidate) {
        key = product.getId() + "|" + timesheetLine.getEmployee().getId();
        if (timeSheetInformationsMap.containsKey(key)) {
          tabInformations = timeSheetInformationsMap.get(key);
          // Update date
          if (timesheetLine.getDate().compareTo((LocalDate) tabInformations[2]) < 0) {
            // If date is lower than start date then replace start date by this one
            tabInformations[2] = timesheetLine.getDate();
          } else if (timesheetLine.getDate().compareTo((LocalDate) tabInformations[3]) > 0) {
            // If date is upper than end date then replace end date by this one
            tabInformations[3] = timesheetLine.getDate();
          }
          tabInformations[4] =
              ((BigDecimal) tabInformations[4]).add(timesheetLine.getHoursDuration());
        } else {
          timeSheetInformationsMap.put(key, tabInformations);
        }
      } else {
        key = String.valueOf(timesheetLine.getId());
        timeSheetInformationsMap.put(key, tabInformations);
      }

      timesheetLine.setInvoiced(true);
    }

    for (Object[] timesheetInformations : timeSheetInformationsMap.values()) {

      String strDate = null;
      Product product = (Product) timesheetInformations[0];
      Employee employee = (Employee) timesheetInformations[1];
      LocalDate startDate = (LocalDate) timesheetInformations[2];
      LocalDate endDate = (LocalDate) timesheetInformations[3];
      BigDecimal hoursDuration = (BigDecimal) timesheetInformations[4];
      PriceList priceList =
          partnerPriceListService.getDefaultPriceList(
              invoice.getPartner(), PriceListRepository.TYPE_SALE);

      if (consolidate) {
        strDate = ddmmFormat.format(startDate) + " - " + ddmmFormat.format(endDate);
      } else {
        strDate = ddmmFormat.format(startDate);
      }

      invoiceLineList.addAll(
          this.createInvoiceLine(
              invoice,
              product,
              null,
              employee,
              strDate,
              hoursDuration,
              priority * 100 + count,
              priceList,
              null,
              null));
      count++;
    }

    return invoiceLineList;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice,
      Product product,
      Project project,
      Employee employee,
      String date,
      BigDecimal hoursDuration,
      int priority,
      PriceList priceList,
      BigDecimal forcedUnitPrice,
      BigDecimal forcedPriceDiscounted)
      throws AxelorException {

    int discountMethodTypeSelect = PriceListLineRepository.TYPE_DISCOUNT;
    int discountTypeSelect = PriceListLineRepository.AMOUNT_TYPE_NONE;
    if (product == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_PRODUCT));
    }
    BigDecimal price =
        (BigDecimal) productCompanyService.get(product, "salePrice", invoice.getCompany());
    BigDecimal discountAmount = BigDecimal.ZERO;
    BigDecimal priceDiscounted = price;

    BigDecimal qtyConverted;
    if (project != null) {
      qtyConverted =
          unitConversionForProjectService.convert(
              appHumanResourceService.getAppBase().getUnitHours(),
              (Unit) productCompanyService.get(product, "unit", invoice.getCompany()),
              hoursDuration,
              AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
              project);
    } else {
      qtyConverted =
          unitConversionService.convert(
              appHumanResourceService.getAppBase().getUnitHours(),
              (Unit) productCompanyService.get(product, "unit", invoice.getCompany()),
              hoursDuration,
              AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
              product);
    }

    if (forcedUnitPrice == null && priceList != null) {
      PriceListLine priceListLine =
          priceListService.getPriceListLine(product, qtyConverted, priceList, price);
      if (priceListLine != null) {
        discountMethodTypeSelect = priceListLine.getTypeSelect();
      }

      Map<String, Object> discounts =
          priceListService.getDiscounts(priceList, priceListLine, price);
      if (discounts != null) {
        discountAmount = (BigDecimal) discounts.get("discountAmount");
        discountTypeSelect = (int) discounts.get("discountTypeSelect");
        priceDiscounted =
            priceListService.computeDiscount(price, discountTypeSelect, discountAmount);
      }

      if ((appHumanResourceService.getAppBase().getComputeMethodDiscountSelect()
                  == AppBaseRepository.INCLUDE_DISCOUNT_REPLACE_ONLY
              && discountMethodTypeSelect == PriceListLineRepository.TYPE_REPLACE)
          || appHumanResourceService.getAppBase().getComputeMethodDiscountSelect()
              == AppBaseRepository.INCLUDE_DISCOUNT) {

        discountTypeSelect = PriceListLineRepository.AMOUNT_TYPE_NONE;
        price = priceDiscounted;
      }
    }

    String description = employee.getName();
    String productName = (String) productCompanyService.get(product, "name", invoice.getCompany());
    if (date != null) {
      productName += " " + "(" + date + ")";
    }

    BigDecimal finalPrice = Optional.ofNullable(forcedUnitPrice).orElse(price);
    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            product,
            productName,
            finalPrice,
            finalPrice,
            Optional.ofNullable(forcedPriceDiscounted).orElse(priceDiscounted),
            description,
            qtyConverted,
            (Unit) productCompanyService.get(product, "unit", invoice.getCompany()),
            null,
            priority,
            discountAmount,
            discountTypeSelect,
            finalPrice.multiply(qtyConverted),
            null,
            false) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }
}
