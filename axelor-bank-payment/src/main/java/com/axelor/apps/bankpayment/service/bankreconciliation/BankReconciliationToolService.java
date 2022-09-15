package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.base.db.Company;
import java.util.Objects;

public class BankReconciliationToolService {

  public static boolean isForeignCurrency(BankReconciliation bankReconciliation) {
    Objects.requireNonNull(bankReconciliation);
    Company company = bankReconciliation.getCompany();

    if (company != null && company.getCurrency() != null) {
      return !company.getCurrency().equals(bankReconciliation.getCurrency());
    }
    return false;
  }
}
