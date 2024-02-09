package com.axelor.apps.bankpayment.service;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleServiceImpl;
import java.math.BigDecimal;

public class CurrencyScaleServiceBankPaymentImpl extends CurrencyScaleServiceImpl
    implements CurrencyScaleServiceBankPayment {

  @Override
  public BigDecimal getScaledValue(BankReconciliation bankReconciliation, BigDecimal amount) {
    return this.getScaledValue(amount, this.getScale(bankReconciliation.getCurrency()));
  }

  @Override
  public BigDecimal getCompanyScaledValue(
      BankReconciliation bankReconciliation, BigDecimal amount) {
    return this.getScaledValue(amount, this.getCompanyScale(bankReconciliation.getCompany()));
  }

  @Override
  public BigDecimal getScaledValue(
      BankReconciliationLine bankReconciliationLine, BigDecimal amount) {
    return bankReconciliationLine.getBankReconciliation() != null
        ? this.getScaledValue(bankReconciliationLine.getBankReconciliation(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public BigDecimal getCompanyScaledValue(
      BankReconciliationLine bankReconciliationLine, BigDecimal amount) {
    return bankReconciliationLine.getBankReconciliation() != null
        ? this.getCompanyScaledValue(bankReconciliationLine.getBankReconciliation(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public BigDecimal getScaledValue(BankStatementLine bankStatementLine, BigDecimal amount) {
    return this.getScaledValue(amount, this.getScale(bankStatementLine.getCurrency()));
  }

  @Override
  public int getScale(BankReconciliation bankReconciliation) {
    return this.getScale(bankReconciliation.getCurrency());
  }

  @Override
  public int getCompanyScale(BankReconciliation bankReconciliation) {
    return this.getCompanyScale(bankReconciliation.getCompany());
  }

  @Override
  public int getScale(BankReconciliationLine bankReconciliationLine) {
    return bankReconciliationLine.getBankReconciliation() != null
        ? this.getScale(bankReconciliationLine.getBankReconciliation())
        : this.getScale();
  }

  @Override
  public int getCompanyScale(BankReconciliationLine bankReconciliationLine) {
    return bankReconciliationLine.getBankReconciliation() != null
        ? this.getCompanyScale(bankReconciliationLine.getBankReconciliation())
        : this.getScale();
  }

  @Override
  public int getScale(BankStatementLine bankStatementLine) {
    return this.getCurrencyScale(bankStatementLine.getCurrency());
  }

  @Override
  public int getScale(Currency currency) {
    return this.getCurrencyScale(currency);
  }

  @Override
  public int getCompanyScale(Company company) {
    return this.getCompanyCurrencyScale(company);
  }
}
