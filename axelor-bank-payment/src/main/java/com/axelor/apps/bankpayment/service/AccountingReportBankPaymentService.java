package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.AccountingReport;

public interface AccountingReportBankPaymentService {

  public String createDomainForBankDetails(AccountingReport accountingReport);
}
