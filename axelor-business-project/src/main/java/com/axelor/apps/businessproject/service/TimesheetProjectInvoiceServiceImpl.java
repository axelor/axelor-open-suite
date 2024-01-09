package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.timesheet.TimesheetInvoiceServiceImpl;
import com.axelor.apps.project.db.Project;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimesheetProjectInvoiceServiceImpl extends TimesheetInvoiceServiceImpl {

  @Inject
  public TimesheetProjectInvoiceServiceImpl(
      AppHumanResourceService appHumanResourceService,
      PartnerPriceListService partnerPriceListService,
      ProductCompanyService productCompanyService,
      PriceListService priceListService,
      TimesheetProjectService timesheetProjectService) {
    super(
        appHumanResourceService, partnerPriceListService, productCompanyService, priceListService);
    this.timesheetProjectService = timesheetProjectService;
  }

  protected TimesheetProjectService timesheetProjectService;

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<TimesheetLine> timesheetLineList, int priority) throws AxelorException {

    if (!appHumanResourceService.isApp("business-project")) {
      return super.createInvoiceLines(invoice, timesheetLineList, priority);
    }

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
    int count = 0;
    DateTimeFormatter ddmmFormat = DateTimeFormatter.ofPattern("dd/MM");
    HashMap<String, Object[]> timeSheetInformationsMap = new HashMap<String, Object[]>();
    // Check if a consolidation by product and user must be done
    boolean consolidate = appHumanResourceService.getAppTimesheet().getConsolidateTSLine();

    for (TimesheetLine timesheetLine : timesheetLineList) {
      Object[] tabInformations = new Object[6];
      Product product = timesheetLine.getProduct();
      tabInformations[0] = product;
      tabInformations[1] = timesheetLine.getEmployee();
      // Start date
      tabInformations[2] = timesheetLine.getDate();
      // End date, useful only for consolidation
      tabInformations[3] = timesheetLine.getDate();
      tabInformations[4] =
          timesheetLine.getDurationForCustomer() != null
              ? timesheetProjectService.computeDurationForCustomer(timesheetLine)
              : timesheetLine.getHoursDuration();
      tabInformations[5] = timesheetLine.getProject();

      String key = null;
      if (consolidate) {
        key =
            (product != null ? product.getId() + "|" : "")
                + timesheetLine.getEmployee().getId()
                + "|"
                + timesheetLine.getProject().getId();
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
              ((BigDecimal) tabInformations[4])
                  .add(
                      timesheetLine.getDurationForCustomer() != null
                          ? timesheetProjectService.computeDurationForCustomer(timesheetLine)
                          : timesheetLine.getHoursDuration());
        } else {
          timeSheetInformationsMap.put(key, tabInformations);
        }
      } else {
        key = String.valueOf(timesheetLine.getId());
        timeSheetInformationsMap.put(key, tabInformations);
      }
    }

    for (Object[] timesheetInformations : timeSheetInformationsMap.values()) {

      String strDate = null;
      Product product = (Product) timesheetInformations[0];
      Employee employee = (Employee) timesheetInformations[1];
      LocalDate startDate = (LocalDate) timesheetInformations[2];
      LocalDate endDate = (LocalDate) timesheetInformations[3];
      BigDecimal hoursDuration = (BigDecimal) timesheetInformations[4];
      Project project = (Project) timesheetInformations[5];
      PriceList priceList = project.getPriceList();
      if (consolidate) {
        if (startDate != null && endDate != null) {
          strDate = startDate.format(ddmmFormat) + " - " + endDate.format(ddmmFormat);
        }
      } else {
        if (startDate != null) {
          strDate = startDate.format(ddmmFormat);
        }
      }

      invoiceLineList.addAll(
          this.createInvoiceLine(
              invoice,
              product,
              employee,
              strDate,
              hoursDuration,
              priority * 100 + count,
              priceList));
      invoiceLineList.get(invoiceLineList.size() - 1).setProject(project);
      count++;
    }

    return invoiceLineList;
  }
}
