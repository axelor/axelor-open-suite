/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImpl;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimesheetProjectServiceImpl extends TimesheetServiceImpl
    implements TimesheetProjectService {

  @Inject
  public TimesheetProjectServiceImpl(
      PriceListService priceListService,
      AppHumanResourceService appHumanResourceService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      ProjectRepository projectRepo,
      UserRepository userRepo,
      UserHrService userHrService,
      TimesheetLineService timesheetLineService,
      ProjectPlanningTimeRepository projectPlanningTimeRepository,
      ProjectTaskRepository projectTaskRepo,
      ProductCompanyService productCompanyService,
      TimesheetLineRepository timesheetlineRepo,
      TimesheetRepository timeSheetRepository,
      ProjectService projectService) {
    super(
        priceListService,
        appHumanResourceService,
        hrConfigService,
        templateMessageService,
        projectRepo,
        userRepo,
        userHrService,
        timesheetLineService,
        projectPlanningTimeRepository,
        projectTaskRepo,
        productCompanyService,
        timesheetlineRepo,
        timeSheetRepository,
        projectService);
  }

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
      Product product = getProduct(timesheetLine);
      tabInformations[0] = product;
      tabInformations[1] = timesheetLine.getEmployee();
      // Start date
      tabInformations[2] = timesheetLine.getDate();
      // End date, useful only for consolidation
      tabInformations[3] = timesheetLine.getDate();
      tabInformations[4] =
          timesheetLine.getDurationForCustomer() != null
              ? this.computeDurationForCustomer(timesheetLine)
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
                          ? this.computeDurationForCustomer(timesheetLine)
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

  @Override
  protected Product getProduct(TimesheetLine timesheetLine) {
    Product product = super.getProduct(timesheetLine);

    if (product == null && timesheetLine.getProjectTask() != null) {
      product = timesheetLine.getProjectTask().getProduct();
    }

    return product;
  }

  @Override
  public BigDecimal computeDurationForCustomer(TimesheetLine timesheetLine) throws AxelorException {
    return timesheetLineService.computeHoursDuration(
        timesheetLine.getTimesheet(), timesheetLine.getDurationForCustomer(), true);
  }

  @Override
  protected TimesheetLine createTimeSheetLineFromPPT(
      Timesheet timesheet, ProjectPlanningTime projectPlanningTime) throws AxelorException {
    TimesheetLine line = super.createTimeSheetLineFromPPT(timesheet, projectPlanningTime);
    if (ObjectUtils.notEmpty(projectPlanningTime.getProjectTask())
        && projectPlanningTime.getProjectTask().getInvoicingType()
            == ProjectTaskRepository.INVOICING_TYPE_TIME_SPENT) {
      line.setToInvoice(projectPlanningTime.getProjectTask().getToInvoice());
    }
    return line;
  }
}
