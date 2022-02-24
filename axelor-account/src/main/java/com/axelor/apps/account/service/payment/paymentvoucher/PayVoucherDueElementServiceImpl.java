package com.axelor.apps.account.service.payment.paymentvoucher;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PayVoucherDueElement;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PayVoucherDueElementRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import javax.inject.Inject;

public class PayVoucherDueElementServiceImpl implements PayVoucherDueElementService {

  protected PayVoucherDueElementRepository payVoucherDueElementRepository;
  protected AppBaseService appBaseService;
  protected AccountConfigService accountConfigService;
  protected AppAccountService appAccountService;
  protected InvoiceTermService invoiceTermService;

  @Inject
  public PayVoucherDueElementServiceImpl(
      PayVoucherDueElementRepository payVoucherDueElementRepository,
      AppBaseService appBaseService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      InvoiceTermService invoiceTermService) {
    this.payVoucherDueElementRepository = payVoucherDueElementRepository;
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
    this.invoiceTermService = invoiceTermService;
  }

  @Override
  @Transactional
  public PayVoucherDueElement updateDueElementWithFinancialDiscount(
      PayVoucherDueElement payVoucherDueElement, PaymentVoucher paymentVoucher) {
    payVoucherDueElement.setPaymentVoucher(paymentVoucher);
    InvoiceTerm invoiceTerm = payVoucherDueElement.getInvoiceTerm();

    if (invoiceTerm.getFinancialDiscount() != null
        && invoiceTerm.getFinancialDiscountDeadlineDate() != null
        && invoiceTerm.getFinancialDiscountDeadlineDate().compareTo(paymentVoucher.getPaymentDate())
            >= 0) {
      payVoucherDueElement.setApplyFinancialDiscount(true);
      payVoucherDueElement.setFinancialDiscount(invoiceTerm.getFinancialDiscount());
      payVoucherDueElement.setFinancialDiscountTotalAmount(
          invoiceTerm.getFinancialDiscountAmount());
      payVoucherDueElement.setFinancialDiscountTaxAmount(
          invoiceTermService.getFinancialDiscountTaxAmount(invoiceTerm));
      payVoucherDueElement.setFinancialDiscountAmount(
          payVoucherDueElement
              .getFinancialDiscountTotalAmount()
              .subtract(payVoucherDueElement.getFinancialDiscountTaxAmount()));
      payVoucherDueElement.setAmountRemainingFinDiscountDeducted(
          invoiceTerm.getRemainingAmountAfterFinDiscount());
      payVoucherDueElement.setFinancialDiscountDeadlineDate(
          invoiceTerm.getFinancialDiscountDeadlineDate());
    }

    return payVoucherDueElement;
  }

  @Override
  public PayVoucherDueElement updateAmounts(PayVoucherDueElement payVoucherDueElement) {
    if (payVoucherDueElement != null && !payVoucherDueElement.getApplyFinancialDiscount()) {
      payVoucherDueElement.setAmountRemainingFinDiscountDeducted(
          payVoucherDueElement.getDueAmount().subtract(payVoucherDueElement.getPaidAmount()));
      payVoucherDueElement.setFinancialDiscountAmount(BigDecimal.ZERO);
      payVoucherDueElement.setFinancialDiscountTaxAmount(BigDecimal.ZERO);
      payVoucherDueElement.setFinancialDiscountTotalAmount(BigDecimal.ZERO);
    } else if (payVoucherDueElement != null
        && payVoucherDueElement.getApplyFinancialDiscount()
        && payVoucherDueElement.getPaymentVoucher() != null) {
      updateDueElementWithFinancialDiscount(
          payVoucherDueElement, payVoucherDueElement.getPaymentVoucher());
    }
    return payVoucherDueElement;
  }
}
