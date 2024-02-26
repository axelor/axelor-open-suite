/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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

  protected int getCompanyCurrencyScale(Company company) {
    return company != null && company.getCurrency() != null
        ? this.getCurrencyScale(company.getCurrency())
        : this.getScale();
  }

  protected int getCurrencyScale(Currency currency) {
    return currency != null ? currency.getNumberOfDecimals() : this.getScale();
  }
}
