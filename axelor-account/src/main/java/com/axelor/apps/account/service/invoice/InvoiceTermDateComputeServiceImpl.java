package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.service.PaymentConditionToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;

public class InvoiceTermDateComputeServiceImpl implements InvoiceTermDateComputeService {

  protected AppAccountService appAccountService;
  protected InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService;
  protected AppBaseService appBaseService;

  @Inject
  public InvoiceTermDateComputeServiceImpl(
      AppAccountService appAccountService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      AppBaseService appBaseService) {
    this.appAccountService = appAccountService;
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
    this.appBaseService = appBaseService;
  }

  @Override
  public LocalDate getInvoiceDateForTermGeneration(Invoice invoice) throws AxelorException {
    if (invoice == null) {
      return null;
    }

    LocalDate invoiceDate = appBaseService.getTodayDate(invoice.getCompany());
    if (InvoiceToolService.isPurchase(invoice) && invoice.getOriginDate() != null) {
      invoiceDate = invoice.getOriginDate();
    } else if (!InvoiceToolService.isPurchase(invoice) && invoice.getInvoiceDate() != null) {
      invoiceDate = invoice.getInvoiceDate();
    }

    return invoiceDate;
  }

  @Override
  public void computeDueDateValues(InvoiceTerm invoiceTerm, LocalDate invoiceDate) {
    LocalDate dueDate =
        PaymentConditionToolService.getDueDate(invoiceTerm.getPaymentConditionLine(), invoiceDate);
    invoiceTerm.setDueDate(dueDate);

    if (appAccountService.getAppAccount().getManageFinancialDiscount()
        && invoiceTerm.getApplyFinancialDiscount()
        && invoiceTerm.getFinancialDiscount() != null) {
      invoiceTerm.setFinancialDiscountDeadlineDate(
          invoiceTermFinancialDiscountService.computeFinancialDiscountDeadlineDate(invoiceTerm));
    }
  }

  @Override
  public void resetDueDate(InvoiceTerm invoiceTerm) throws AxelorException {
    Optional<Invoice> invoiceOpt = Optional.ofNullable(invoiceTerm).map(InvoiceTerm::getInvoice);
    if (invoiceOpt.isEmpty()) {
      return;
    }

    LocalDate invoiceDate = getInvoiceDateForTermGeneration(invoiceOpt.get());
    if (invoiceDate != null) {
      computeDueDateValues(invoiceTerm, invoiceDate);
    }
  }
}
