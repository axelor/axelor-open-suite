package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.invoiceterm.InvoiceTermDomainServiceImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

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
    String domain = "";

    PaymentMode paymentMode = invoiceTerm.getPaymentMode();
    Company company = invoiceTerm.getCompany();

    if (partner != null && !partner.getBankDetailsList().isEmpty()) {
      List<BankDetails> bankDetailsList =
          bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
              paymentMode, partner, company);
      if (bankDetailsList.isEmpty()) {
        bankDetailsList =
            partner.getBankDetailsList().stream()
                .filter(BankDetails::getActive)
                .collect(Collectors.toList());
      }
      List<Long> bankDetailsIdList =
          bankDetailsList.stream().map(BankDetails::getId).collect(Collectors.toList());
      if (!bankDetailsIdList.isEmpty()) {
        domain = "self.id IN (" + StringUtils.join(bankDetailsIdList, ',') + ")";
      }
    }
    return domain;
  }
}
