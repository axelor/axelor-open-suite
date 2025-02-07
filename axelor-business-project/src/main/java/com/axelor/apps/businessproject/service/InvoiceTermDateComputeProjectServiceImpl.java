package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermDateComputeServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceTermFinancialDiscountService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class InvoiceTermDateComputeProjectServiceImpl extends InvoiceTermDateComputeServiceImpl {

  @Inject
  public InvoiceTermDateComputeProjectServiceImpl(
      AppAccountService appAccountService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      AppBaseService appBaseService) {
    super(appAccountService, invoiceTermFinancialDiscountService, appBaseService);
  }

  @Override
  public void computeDueDateValues(InvoiceTerm invoiceTerm, LocalDate invoiceDate) {
    Optional<LocalDate> projectToDate =
        Optional.of(invoiceTerm)
            .map(InvoiceTerm::getInvoice)
            .map(Invoice::getProject)
            .map(Project::getToDate)
            .map(LocalDateTime::toLocalDate);
    if (projectToDate.isPresent() && invoiceTerm.getIsHoldBack()) {
      invoiceDate = projectToDate.get();
    }
    super.computeDueDateValues(invoiceTerm, invoiceDate);
  }
}
