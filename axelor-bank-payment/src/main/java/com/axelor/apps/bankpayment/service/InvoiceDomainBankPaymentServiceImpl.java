package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.FiscalPositionRepository;
import com.axelor.apps.account.service.invoice.InvoiceDomainServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;

public class InvoiceDomainBankPaymentServiceImpl extends InvoiceDomainServiceImpl {
  protected final BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public InvoiceDomainBankPaymentServiceImpl(
      InvoiceService invoiceService,
      FiscalPositionRepository fiscalPositionRepository,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(invoiceService, fiscalPositionRepository);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public String createDomainForBankDetails(Invoice invoice) {
    Partner partner = invoice.getPartner();
    String domain = super.createDomainForBankDetails(invoice);

    PaymentMode paymentMode = invoice.getPaymentMode();
    Company company = invoice.getCompany();

    if (partner != null && !partner.getBankDetailsList().isEmpty()) {
      List<BankDetails> bankDetailsList =
          bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
              paymentMode, partner, company);
      if (!bankDetailsList.isEmpty()) {
        domain = "self.id IN (" + StringHelper.getIdListString(bankDetailsList) + ")";
      }
    }
    return domain;
  }
}
