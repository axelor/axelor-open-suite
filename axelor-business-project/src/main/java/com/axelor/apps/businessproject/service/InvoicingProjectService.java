/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.account.util.InvoiceLineComparator;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.businessproject.db.ElementsToInvoice;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.ElementsToInvoiceRepository;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineGeneratorSupplyChain;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InvoicingProjectService {

  @Inject protected TimesheetServiceImpl timesheetServiceImpl;

  @Inject protected ExpenseService expenseService;

  @Inject protected ElementsToInvoiceService elementsToInvoiceService;

  @Inject protected PartnerService partnerService;

  @Inject protected InvoicingProjectRepository invoicingProjectRepo;

  protected int MAX_LEVEL_OF_PROJECT = 10;

  protected int sequence = 0;

  @Transactional
  public Invoice generateInvoice(InvoicingProject invoicingProject) throws AxelorException {
    Project project = invoicingProject.getProject();
    Partner customer = project.getClientPartner();
    Company company = this.getRootCompany(project);
    if (company == null) {
      throw new AxelorException(
          invoicingProject,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INVOICING_PROJECT_PROJECT_COMPANY));
    }
    project.getAssignedTo();
    InvoiceGenerator invoiceGenerator =
        new InvoiceGenerator(
            InvoiceRepository.OPERATION_TYPE_CLIENT_SALE,
            company,
            customer.getPaymentCondition(),
            customer.getInPaymentMode(),
            partnerService.getInvoicingAddress(customer),
            customer,
            null,
            customer.getCurrency(),
            Beans.get(PartnerPriceListService.class)
                .getDefaultPriceList(customer, PriceListRepository.TYPE_SALE),
            null,
            null,
            null,
            null,
            null) {

          @Override
          public Invoice generate() throws AxelorException {

            return super.createInvoiceHeader();
          }
        };
    Invoice invoice = invoiceGenerator.generate();
    AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    invoice.setDisplayTimesheetOnPrinting(accountConfig.getDisplayTimesheetOnPrinting());
    invoice.setDisplayExpenseOnPrinting(accountConfig.getDisplayExpenseOnPrinting());

    invoiceGenerator.populate(invoice, this.populate(invoice, invoicingProject));
    Beans.get(InvoiceRepository.class).save(invoice);

    this.setInvoiced(invoicingProject);
    invoicingProject.setInvoice(invoice);
    invoicingProjectRepo.save(invoicingProject);
    return invoice;
  }

  public List<InvoiceLine> populate(Invoice invoice, InvoicingProject folder)
      throws AxelorException {
    List<SaleOrderLine> saleOrderLineList =
        new ArrayList<SaleOrderLine>(folder.getSaleOrderLineSet());
    List<PurchaseOrderLine> purchaseOrderLineList =
        new ArrayList<PurchaseOrderLine>(folder.getPurchaseOrderLineSet());
    List<TimesheetLine> timesheetLineList = new ArrayList<TimesheetLine>(folder.getLogTimesSet());
    List<ExpenseLine> expenseLineList = new ArrayList<ExpenseLine>(folder.getExpenseLineSet());
    List<ElementsToInvoice> elementsToInvoiceList =
        new ArrayList<ElementsToInvoice>(folder.getElementsToInvoiceSet());
    List<Project> projectList = new ArrayList<Project>(folder.getProjectSet());

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
    invoiceLineList.addAll(
        this.createSaleOrderInvoiceLines(
            invoice, saleOrderLineList, folder.getSaleOrderLineSetPrioritySelect()));
    invoiceLineList.addAll(
        this.createPurchaseOrderInvoiceLines(
            invoice, purchaseOrderLineList, folder.getPurchaseOrderLineSetPrioritySelect()));
    invoiceLineList.addAll(
        timesheetServiceImpl.createInvoiceLines(
            invoice, timesheetLineList, folder.getLogTimesSetPrioritySelect()));
    invoiceLineList.addAll(
        expenseService.createInvoiceLines(
            invoice, expenseLineList, folder.getExpenseLineSetPrioritySelect()));
    invoiceLineList.addAll(
        elementsToInvoiceService.createInvoiceLines(
            invoice, elementsToInvoiceList, folder.getElementsToInvoiceSetPrioritySelect()));
    invoiceLineList.addAll(
        this.createInvoiceLines(invoice, projectList, folder.getProjectSetPrioritySelect()));

    Collections.sort(invoiceLineList, new InvoiceLineComparator());

    for (InvoiceLine invoiceLine : invoiceLineList) {
      invoiceLine.setSequence(sequence);
      sequence++;
      invoiceLine.setSaleOrder(invoiceLine.getInvoice().getSaleOrder());
    }

    return invoiceLineList;
  }

  public List<InvoiceLine> createSaleOrderInvoiceLines(
      Invoice invoice, List<SaleOrderLine> saleOrderLineList, int priority) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
    int count = 1;
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {

      invoiceLineList.addAll(
          this.createInvoiceLine(invoice, saleOrderLine, priority * 100 + count));
      count++;
      saleOrderLine.setInvoiced(true);
    }

    return invoiceLineList;
  }

  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice, SaleOrderLine saleOrderLine, int priority) throws AxelorException {

    Product product = saleOrderLine.getProduct();

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGeneratorSupplyChain(
            invoice,
            product,
            saleOrderLine.getProductName(),
            saleOrderLine.getDescription(),
            saleOrderLine.getQty(),
            saleOrderLine.getUnit(),
            priority,
            false,
            saleOrderLine,
            null,
            null) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }

  public List<InvoiceLine> createPurchaseOrderInvoiceLines(
      Invoice invoice, List<PurchaseOrderLine> purchaseOrderLineList, int priority)
      throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {

      invoiceLineList.addAll(
          Beans.get(PurchaseOrderInvoiceProjectServiceImpl.class)
              .createInvoiceLine(invoice, purchaseOrderLine));
      purchaseOrderLine.setInvoiced(true);
    }
    return invoiceLineList;
  }

  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice, PurchaseOrderLine purchaseOrderLine, int priority) throws AxelorException {

    Product product = purchaseOrderLine.getProduct();

    InvoiceLineGeneratorSupplyChain invoiceLineGenerator =
        new InvoiceLineGeneratorSupplyChain(
            invoice,
            product,
            purchaseOrderLine.getProductName(),
            purchaseOrderLine.getDescription(),
            purchaseOrderLine.getQty(),
            purchaseOrderLine.getUnit(),
            priority,
            false,
            null,
            purchaseOrderLine,
            null) {
          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }

  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<Project> projectList, int priority) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
    int count = 0;
    for (Project project : projectList) {

      invoiceLineList.addAll(this.createInvoiceLine(invoice, project, priority * 100 + count));
      count++;
      project.setInvoiced(true);
      invoiceLineList.get(invoiceLineList.size() - 1).setProject(project);
    }

    return invoiceLineList;
  }

  public List<InvoiceLine> createInvoiceLine(Invoice invoice, Project project, int priority)
      throws AxelorException {

    Product product = project.getProduct();

    if (product == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INVOICING_PROJECT_PROJECT_PRODUCT),
          project.getFullName());
    }

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            product,
            project.getName(),
            project.getPrice(),
            project.getPrice(),
            project.getPrice(),
            null,
            project.getQty(),
            project.getUnit(),
            null,
            priority,
            BigDecimal.ZERO,
            PriceListLineRepository.AMOUNT_TYPE_NONE,
            project.getPrice().multiply(project.getQty()),
            null,
            false) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }

  public void setInvoiced(InvoicingProject invoicingProject) {
    for (SaleOrderLine saleOrderLine : invoicingProject.getSaleOrderLineSet()) {
      saleOrderLine.setInvoiced(true);
    }
    for (PurchaseOrderLine purchaseOrderLine : invoicingProject.getPurchaseOrderLineSet()) {
      purchaseOrderLine.setInvoiced(true);
    }
    for (TimesheetLine timesheetLine : invoicingProject.getLogTimesSet()) {
      timesheetLine.setInvoiced(true);
    }
    for (ExpenseLine expenseLine : invoicingProject.getExpenseLineSet()) {
      expenseLine.setInvoiced(true);
    }
    for (ElementsToInvoice elementsToInvoice : invoicingProject.getElementsToInvoiceSet()) {
      elementsToInvoice.setInvoiced(true);
    }
    for (Project project : invoicingProject.getProjectSet()) {
      project.setInvoiced(true);
    }
  }

  public void setLines(InvoicingProject invoicingProject, Project project, int counter) {

    if (counter > ProjectServiceImpl.MAX_LEVEL_OF_PROJECT) {
      return;
    }
    counter++;

    this.fillLines(invoicingProject, project);

    List<Project> projectChildrenList =
        Beans.get(ProjectRepository.class).all().filter("self.parentProject = ?1", project).fetch();

    for (Project projectChild : projectChildrenList) {
      this.setLines(invoicingProject, projectChild, counter);
    }

    return;
  }

  public void fillLines(InvoicingProject invoicingProject, Project project) {
    if (project.getProjInvTypeSelect() == ProjectRepository.INVOICING_TYPE_FLAT_RATE
        || project.getProjInvTypeSelect() == ProjectRepository.INVOICING_TYPE_TIME_BASED) {
      if (invoicingProject.getDeadlineDate() != null) {
        invoicingProject
            .getSaleOrderLineSet()
            .addAll(
                Beans.get(SaleOrderLineRepository.class)
                    .all()
                    .filter(
                        "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false AND self.saleOrder.creationDate < ?2",
                        project,
                        invoicingProject.getDeadlineDate())
                    .fetch());

        invoicingProject
            .getPurchaseOrderLineSet()
            .addAll(
                Beans.get(PurchaseOrderLineRepository.class)
                    .all()
                    .filter(
                        "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false AND self.purchaseOrder.orderDate < ?2",
                        project,
                        invoicingProject.getDeadlineDate())
                    .fetch());

        invoicingProject
            .getLogTimesSet()
            .addAll(
                Beans.get(TimesheetLineRepository.class)
                    .all()
                    .filter(
                        "self.timesheet.statusSelect = 3 AND self.project = ?1 AND self.toInvoice = true AND self.invoiced = false AND self.date < ?2",
                        project,
                        invoicingProject.getDeadlineDate())
                    .fetch());

        invoicingProject
            .getExpenseLineSet()
            .addAll(
                Beans.get(ExpenseLineRepository.class)
                    .all()
                    .filter(
                        "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false AND self.expenseDate < ?2",
                        project,
                        invoicingProject.getDeadlineDate())
                    .fetch());

        invoicingProject
            .getElementsToInvoiceSet()
            .addAll(
                Beans.get(ElementsToInvoiceRepository.class)
                    .all()
                    .filter(
                        "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false AND self.date < ?2",
                        project,
                        invoicingProject.getDeadlineDate())
                    .fetch());
      } else {
        invoicingProject
            .getSaleOrderLineSet()
            .addAll(
                Beans.get(SaleOrderLineRepository.class)
                    .all()
                    .filter(
                        "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false",
                        project)
                    .fetch());

        invoicingProject
            .getPurchaseOrderLineSet()
            .addAll(
                Beans.get(PurchaseOrderLineRepository.class)
                    .all()
                    .filter(
                        "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false",
                        project)
                    .fetch());

        invoicingProject
            .getLogTimesSet()
            .addAll(
                Beans.get(TimesheetLineRepository.class)
                    .all()
                    .filter(
                        "self.timesheet.statusSelect = 3 AND self.project = ?1 AND self.toInvoice = true AND self.invoiced = false",
                        project)
                    .fetch());

        invoicingProject
            .getExpenseLineSet()
            .addAll(
                Beans.get(ExpenseLineRepository.class)
                    .all()
                    .filter(
                        "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false",
                        project)
                    .fetch());

        invoicingProject
            .getElementsToInvoiceSet()
            .addAll(
                Beans.get(ElementsToInvoiceRepository.class)
                    .all()
                    .filter(
                        "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false",
                        project)
                    .fetch());
      }
      if (project.getProjInvTypeSelect() == ProjectRepository.INVOICING_TYPE_FLAT_RATE
          && !project.getInvoiced()) invoicingProject.addProjectSetItem(project);
    }
  }

  public void clearLines(InvoicingProject invoicingProject) {

    invoicingProject.setSaleOrderLineSet(new HashSet<SaleOrderLine>());
    invoicingProject.setPurchaseOrderLineSet(new HashSet<PurchaseOrderLine>());
    invoicingProject.setLogTimesSet(new HashSet<TimesheetLine>());
    invoicingProject.setExpenseLineSet(new HashSet<ExpenseLine>());
    invoicingProject.setElementsToInvoiceSet(new HashSet<ElementsToInvoice>());
    invoicingProject.setProjectSet(new HashSet<Project>());
  }

  public Company getRootCompany(Project project) {
    if (project.getParentProject() == null) {
      return project.getCompany();
    } else {
      return getRootCompany(project.getParentProject());
    }
  }

  @Transactional
  public InvoicingProject createInvoicingProject(
      SaleOrder saleOrder, LocalDate deadlineDate, int invoicingType) {

    InvoicingProject invoicingProject = new InvoicingProject();
    invoicingProject.setDeadlineDate(deadlineDate);

    Project project = saleOrder.getProject();
    invoicingProject.setProject(project);

    Set<SaleOrderLine> saleOrderLineList =
        new HashSet<>(
            Beans.get(SaleOrderLineRepository.class)
                .all()
                .filter(
                    "self.saleOrder.id = :_saleOrderId"
                        + " AND self.toInvoice = true"
                        + " AND self.invoiced = false"
                        + " AND self.saleOrder.creationDate < :_deadlineDate")
                .bind("_saleOrderId", saleOrder.getId())
                .bind("_deadlineDate", deadlineDate)
                .fetch());
    invoicingProject.setSaleOrderLineSet(saleOrderLineList);

    if (invoicingType == SaleOrderRepository.INVOICE_TIMESHEET) {
      Set<TimesheetLine> timesheetLineList =
          new HashSet<>(
              Beans.get(TimesheetLineRepository.class)
                  .all()
                  .filter(
                      "self.timesheet.statusSelect = :_statusValidated"
                          + " AND self.project = :_project"
                          + " AND self.toInvoice = true"
                          + " AND self.invoiced = false"
                          + " AND self.date < :_deadlineDate")
                  .bind("_statusValidated", TimesheetRepository.STATUS_VALIDATED)
                  .bind("_project", project)
                  .bind("_deadlineDate", deadlineDate)
                  .fetch());
      invoicingProject.setLogTimesSet(timesheetLineList);
    } else if (invoicingType == SaleOrderRepository.INVOICE_EXPENSE) {
      Set<ExpenseLine> expenseLineList =
          new HashSet<>(
              Beans.get(ExpenseLineRepository.class)
                  .all()
                  .filter(
                      "self.project = _project"
                          + " AND self.toInvoice = true"
                          + " AND self.invoiced = false"
                          + " AND self.expenseDate < :_deadlineDate")
                  .bind("_project", project)
                  .bind("_deadlineDate", deadlineDate)
                  .fetch());
      invoicingProject.setExpenseLineSet(expenseLineList);
    }

    return invoicingProjectRepo.save(invoicingProject);
  }

  public int countToInvoice(Project project) {

    int toInvoiceCount = 0;

    toInvoiceCount +=
        Beans.get(SaleOrderLineRepository.class)
            .all()
            .filter(
                "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false", project)
            .count();

    toInvoiceCount +=
        Beans.get(PurchaseOrderLineRepository.class)
            .all()
            .filter(
                "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false", project)
            .count();

    toInvoiceCount +=
        Beans.get(ExpenseLineRepository.class)
            .all()
            .filter(
                "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false", project)
            .count();

    toInvoiceCount +=
        Beans.get(TimesheetLineRepository.class)
            .all()
            .filter(
                "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false", project)
            .count();

    toInvoiceCount +=
        Beans.get(ElementsToInvoiceRepository.class)
            .all()
            .filter(
                "self.project = ?1 AND self.toInvoice = true AND self.invoiced = false", project)
            .count();

    return toInvoiceCount;
  }
}
