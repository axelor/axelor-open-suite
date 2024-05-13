package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class ProjectGenerateInvoiceServiceImpl implements ProjectGenerateInvoiceService {

  protected InvoicingProjectService invoicingProjectService;
  protected PartnerService partnerService;
  protected InvoicingProjectRepository invoicingProjectRepo;
  protected ProjectHoldBackLineService projectHoldBackLineService;

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
            project);
    return createInvoice(invoicingProject, invoiceGenerator, company);
  }

  protected Invoice createInvoice(
      InvoicingProject invoicingProject, InvoiceGenerator invoiceGenerator, Company company)
      throws AxelorException {
    Invoice invoice = invoiceGenerator.generate();
    AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    invoice.setDisplayTimesheetOnPrinting(accountConfig.getDisplayTimesheetOnPrinting());
    invoice.setDisplayExpenseOnPrinting(accountConfig.getDisplayExpenseOnPrinting());

    invoiceGenerator.populate(invoice, invoicingProjectService.populate(invoice, invoicingProject));
    invoice = projectHoldBackLineService.generateInvoiceLinesForHoldBacks(invoice);
    Beans.get(InvoiceRepository.class).save(invoice);

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
      Project project)
      throws AxelorException {

    return new InvoiceGenerator(
        operationTypeSelect,
        company,
        invoicedPartner.getPaymentCondition(),
        invoicedPartner.getInPaymentMode(),
        partnerService.getInvoicingAddress(invoicedPartner),
        invoicedPartner,
        invoicedPartnerContact,
        invoicedPartner.getCurrency(),
        Beans.get(PartnerPriceListService.class)
            .getDefaultPriceList(invoicedPartner, PriceListRepository.TYPE_SALE),
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
}
