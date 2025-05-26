package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.invoiceterm.InvoiceTermDomainServiceImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;

public class InvoiceTermDomainBankPaymentServiceImpl extends InvoiceTermDomainServiceImpl {
  protected final BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public InvoiceTermDomainBankPaymentServiceImpl(
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public String createDomainForBankDetails(InvoiceTerm invoiceTerm) {
    Partner partner = invoiceTerm.getPartner();
    String domain = super.createDomainForBankDetails(invoiceTerm);

    PaymentMode paymentMode = invoiceTerm.getPaymentMode();
    Company company = invoiceTerm.getCompany();

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
