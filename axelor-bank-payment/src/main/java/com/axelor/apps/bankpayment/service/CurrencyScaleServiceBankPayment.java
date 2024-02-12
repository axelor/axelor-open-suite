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
