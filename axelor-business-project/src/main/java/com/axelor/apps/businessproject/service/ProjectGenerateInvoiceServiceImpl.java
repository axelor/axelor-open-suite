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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.TaxNumberRepository;
import com.axelor.apps.account.service.PartnerAccountService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.account.util.InvoiceLineComparator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskBusinessProjectService;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.expense.ExpenseInvoiceLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetInvoiceService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineGeneratorSupplyChain;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppBusinessProject;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectGenerateInvoiceServiceImpl implements ProjectGenerateInvoiceService {

  protected InvoicingProjectService invoicingProjectService;
  protected PartnerService partnerService;
  protected InvoicingProjectRepository invoicingProjectRepo;
  protected ProjectHoldBackLineService projectHoldBackLineService;
  protected PartnerPriceListService partnerPriceListService;
  protected InvoiceRepository invoiceRepository;
  protected AccountConfigService accountConfigService;
  protected TimesheetInvoiceService timesheetInvoiceService;

  protected ExpenseInvoiceLineService expenseInvoiceLineService;

  protected InvoicingProjectStockMovesService invoicingProjectStockMovesService;
  protected InvoiceLineService invoiceLineService;
  protected ProjectTaskBusinessProjectService projectTaskBusinessProjectService;
  protected AppBusinessProjectService appBusinessProjectService;
  protected InvoiceLineAnalyticService invoiceLineAnalyticService;
  protected AnalyticLineService analyticLineService;
  protected PartnerAccountService partnerAccountService;
  protected TaxNumberRepository taxNumberRepository;

  protected int sequence = 0;

  @Inject
  public ProjectGenerateInvoiceServiceImpl(
      InvoicingProjectService invoicingProjectService,
      PartnerService partnerService,
      InvoicingProjectRepository invoicingProjectRepo,
      ProjectHoldBackLineService projectHoldBackLineService,
      PartnerPriceListService partnerPriceListService,
      InvoiceRepository invoiceRepository,
      AccountConfigService accountConfigService,
      TimesheetInvoiceService timesheetInvoiceService,
      ExpenseInvoiceLineService expenseInvoiceLineService,
      InvoicingProjectStockMovesService invoicingProjectStockMovesService,
      InvoiceLineService invoiceLineService,
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService,
      AppBusinessProjectService appBusinessProjectService,
      InvoiceLineAnalyticService invoiceLineAnalyticService,
      AnalyticLineService analyticLineService,
      PartnerAccountService partnerAccountService,
      TaxNumberRepository taxNumberRepository) {
    this.invoicingProjectService = invoicingProjectService;
    this.partnerService = partnerService;
    this.invoicingProjectRepo = invoicingProjectRepo;
    this.projectHoldBackLineService = projectHoldBackLineService;
    this.partnerPriceListService = partnerPriceListService;
    this.invoiceRepository = invoiceRepository;
    this.accountConfigService = accountConfigService;
    this.timesheetInvoiceService = timesheetInvoiceService;
    this.expenseInvoiceLineService = expenseInvoiceLineService;
    this.invoicingProjectStockMovesService = invoicingProjectStockMovesService;
    this.invoiceLineService = invoiceLineService;
    this.projectTaskBusinessProjectService = projectTaskBusinessProjectService;
    this.appBusinessProjectService = appBusinessProjectService;
    this.invoiceLineAnalyticService = invoiceLineAnalyticService;
    this.analyticLineService = analyticLineService;
    this.partnerAccountService = partnerAccountService;
    this.taxNumberRepository = taxNumberRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Invoice generateInvoice(InvoicingProject invoicingProject) throws AxelorException {

    checks(invoicingProject);

    Project project = invoicingProject.getProject();
    Partner customer = project.getClientPartner();
    Partner customerContact = project.getContactPartner();

    if (customerContact == null && customer.getContactPartnerSet().size() == 1) {
      customerContact = customer.getContactPartnerSet().iterator().next();
    }
    Company company = invoicingProjectService.getRootCompany(project);
    if (company == null) {
      throw new AxelorException(
          invoicingProject,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BusinessProjectExceptionMessage.INVOICING_PROJECT_PROJECT_COMPANY));
    }
    InvoiceGenerator invoiceGenerator =
        getInvoiceGenerator(
            InvoiceRepository.OPERATION_TYPE_CLIENT_SALE,
            company,
            customer,
            customerContact,
            project,
            customer.getInPaymentMode());
    return createInvoice(invoicingProject, invoiceGenerator, company);
  }

  protected Invoice createInvoice(
      InvoicingProject invoicingProject, InvoiceGenerator invoiceGenerator, Company company)
      throws AxelorException {
    Invoice invoice = invoiceGenerator.generate();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    invoice.setDisplayTimesheetOnPrinting(accountConfig.getDisplayTimesheetOnPrinting());
    invoice.setDisplayExpenseOnPrinting(accountConfig.getDisplayExpenseOnPrinting());
    AppBusinessProject appBusinessProject = appBusinessProjectService.getAppBusinessProject();
    invoice.setIsExpenseLineOnInvoiceGrouped(appBusinessProject.getIsExpenseLineOnInvoiceGrouped());
    invoice.setGroupingPeriodSelect(appBusinessProject.getGroupingPeriodSelect());

    Partner companyPartner = company.getPartner();
    if (companyPartner != null && companyPartner.getTaxNbr() != null) {
      TaxNumber companyTaxNumber =
          taxNumberRepository
              .findByCompanyAndTaxNbr(company, companyPartner.getTaxNbr())
              .fetchOne();
      invoice.setCompanyTaxNumber(companyTaxNumber);
    }

    invoiceGenerator.populate(invoice, this.populate(invoice, invoicingProject));
    invoice = projectHoldBackLineService.generateInvoiceLinesForHoldBacks(invoice);
    invoiceRepository.save(invoice);

    invoicingProject.setInvoice(invoice);
    invoicingProject.setStatusSelect(InvoicingProjectRepository.STATUS_GENERATED);
    invoicingProjectRepo.save(invoicingProject);
    return invoice;
  }

  protected InvoiceGenerator getInvoiceGenerator(
      int operationTypeSelect,
      Company company,
      Partner invoicedPartner,
      Partner invoicedPartnerContact,
      Project project,
      PaymentMode paymentMode)
      throws AxelorException {

    return new InvoiceGenerator(
        operationTypeSelect,
        company,
        invoicedPartner.getPaymentCondition(),
        paymentMode,
        partnerService.getInvoicingAddress(invoicedPartner),
        invoicedPartner,
        invoicedPartnerContact,
        invoicedPartner.getCurrency(),
        partnerPriceListService.getDefaultPriceList(invoicedPartner, PriceListRepository.TYPE_SALE),
        null,
        null,
        null,
        null,
        null,
        null,
        null) {

      @Override
      public Invoice generate() throws AxelorException {

        Invoice invoice = super.createInvoiceHeader();
        invoice.setProject(project);
        invoice.setPriceList(project.getPriceList());
        invoice.setFiscalPosition(invoicedPartner.getFiscalPosition());
        invoice.setThirdPartyPayerPartner(partnerAccountService.getPayedByPartner(invoicedPartner));
        return invoice;
      }
    };
  }

  protected void checks(InvoicingProject invoicingProject) throws AxelorException {
    if (invoicingProject.getProject() == null) {
      throw new AxelorException(
          invoicingProject,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BusinessProjectExceptionMessage.INVOICING_PROJECT_PROJECT));
    }

    if (invoicingProject.getSaleOrderLineSet().isEmpty()
        && invoicingProject.getPurchaseOrderLineSet().isEmpty()
        && invoicingProject.getLogTimesSet().isEmpty()
        && invoicingProject.getExpenseLineSet().isEmpty()
        && invoicingProject.getProjectTaskSet().isEmpty()
        && invoicingProject.getStockMoveLineSet().isEmpty()) {
      throw new AxelorException(
          invoicingProject,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BusinessProjectExceptionMessage.INVOICING_PROJECT_EMPTY));
    }

    if (invoicingProject.getProject().getClientPartner() == null) {
      throw new AxelorException(
          invoicingProject,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BusinessProjectExceptionMessage.INVOICING_PROJECT_PROJECT_PARTNER));
    }
  }

  protected List<InvoiceLine> populate(Invoice invoice, InvoicingProject folder)
      throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = new ArrayList<>(folder.getSaleOrderLineSet());
    List<PurchaseOrderLine> purchaseOrderLineList =
        new ArrayList<>(folder.getPurchaseOrderLineSet());
    List<TimesheetLine> timesheetLineList = new ArrayList<>(folder.getLogTimesSet());
    List<ExpenseLine> expenseLineList = new ArrayList<>(folder.getExpenseLineSet());
    List<ProjectTask> projectTaskList = new ArrayList<>(folder.getProjectTaskSet());

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    invoiceLineList.addAll(
        invoicingProjectStockMovesService.createStockMovesInvoiceLines(
            invoice, folder.getStockMoveLineSet()));
    invoiceLineList.addAll(
        this.createSaleOrderInvoiceLines(
            invoice, saleOrderLineList, folder.getSaleOrderLineSetPrioritySelect()));
    invoiceLineList.addAll(
        this.createPurchaseOrderInvoiceLines(
            invoice, purchaseOrderLineList, folder.getPurchaseOrderLineSetPrioritySelect()));
    invoiceLineList.addAll(
        timesheetInvoiceService.createInvoiceLines(
            invoice, timesheetLineList, folder.getLogTimesSetPrioritySelect()));
    invoiceLineList.addAll(
        expenseInvoiceLineService.createInvoiceLines(
            invoice, expenseLineList, folder.getExpenseLineSetPrioritySelect()));
    invoiceLineList.addAll(
        projectTaskBusinessProjectService.createInvoiceLines(
            invoice, projectTaskList, folder.getProjectTaskSetPrioritySelect()));

    Collections.sort(invoiceLineList, new InvoiceLineComparator());

    for (InvoiceLine invoiceLine : invoiceLineList) {
      invoiceLine.setSequence(sequence);
      sequence++;

      this.computeAnalytic(invoiceLine, folder.getProject(), invoice.getCompany());
      invoiceLineService.compute(invoice, invoiceLine);
    }

    return invoiceLineList;
  }

  protected List<InvoiceLine> createSaleOrderInvoiceLines(
      Invoice invoice, List<SaleOrderLine> saleOrderLineList, int priority) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 1;
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {

      invoiceLineList.addAll(
          this.createInvoiceLine(invoice, saleOrderLine, priority * 100 + count));
      count++;
    }

    return invoiceLineList;
  }

  protected List<InvoiceLine> createInvoiceLine(
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
            invoiceLine.setProject(saleOrderLine.getProject());
            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }

  protected List<InvoiceLine> createPurchaseOrderInvoiceLines(
      Invoice invoice, List<PurchaseOrderLine> purchaseOrderLineList, int priority)
      throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {

      invoiceLineList.addAll(
          Beans.get(PurchaseOrderInvoiceProjectServiceImpl.class)
              .createInvoiceLine(invoice, purchaseOrderLine));
    }
    return invoiceLineList;
  }

  protected void computeAnalytic(InvoiceLine invoiceLine, Project project, Company company)
      throws AxelorException {
    if (project != null) {
      invoiceLine.setAnalyticDistributionTemplate(project.getAnalyticDistributionTemplate());

      List<AnalyticMoveLine> analyticMoveLineList =
          invoiceLineAnalyticService.createAnalyticDistributionWithTemplate(invoiceLine);
      analyticMoveLineList.forEach(invoiceLine::addAnalyticMoveLineListItem);
      invoiceLine.setAnalyticMoveLineList(analyticMoveLineList);

      analyticLineService.setAnalyticAccount(invoiceLine, company);
    }
  }
}
