package com.axelor.apps.bankpayment.service;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import java.math.BigDecimal;

public interface CurrencyScaleServiceBankPayment {

  BigDecimal getScaledValue(BankReconciliation bankReconciliation, BigDecimal amount);

  BigDecimal getCompanyScaledValue(BankReconciliation bankReconciliation, BigDecimal amount);

  BigDecimal getScaledValue(BankReconciliationLine bankReconciliationLine, BigDecimal amount);

  BigDecimal getCompanyScaledValue(
      BankReconciliationLine bankReconciliationLine, BigDecimal amount);

  BigDecimal getScaledValue(BankStatementLine bankStatementLine, BigDecimal amount);

  int getScale(BankReconciliation bankReconciliation);

  int getCompanyScale(BankReconciliation bankReconciliation);

  int getScale(BankReconciliationLine bankReconciliationLine);

  int getCompanyScale(BankReconciliationLine bankReconciliationLine);

  int getScale(BankStatementLine bankStatementLine);

  int getScale(Currency currency);

  int getCompanyScale(Company company);
}
