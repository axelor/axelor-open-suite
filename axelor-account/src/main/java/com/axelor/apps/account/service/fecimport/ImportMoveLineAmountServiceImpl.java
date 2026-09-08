/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.fecimport;

import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.util.Optional;

public class ImportMoveLineAmountServiceImpl implements ImportMoveLineAmountService {

  @Override
  public void computeImportedAmounts(
      MoveLine moveLine,
      Object importedCurrency,
      Object importedCurrencyAmount,
      String entryNumber,
      FECImport fecImport)
      throws AxelorException {
    if (moveLine == null) {
      return;
    }

    normalizeDebitCredit(moveLine);

    if (moveLine.getDebit().signum() == 0 && moveLine.getCredit().signum() == 0) {
      moveLine.setCurrencyAmount(BigDecimal.ZERO);
      return;
    }

    moveLine.setCurrencyAmount(
        getCurrencyAmount(
            moveLine, importedCurrency, importedCurrencyAmount, entryNumber, fecImport));
  }

  @Override
  public boolean isCompanyCurrency(Object importedCurrency, Currency companyCurrency) {
    if (companyCurrency == null) {
      return false;
    }

    if (ObjectUtils.isEmpty(importedCurrency)) {
      return true;
    }

    String currencyCode = importedCurrency.toString().trim();
    if (StringUtils.isEmpty(currencyCode) || "0".equals(currencyCode)) {
      return true;
    }

    return currencyCode.equalsIgnoreCase(companyCurrency.getCode())
        || currencyCode.equalsIgnoreCase(companyCurrency.getCodeISO())
        || currencyCode.equalsIgnoreCase(companyCurrency.getName());
  }

  protected Currency getCompanyCurrency(MoveLine moveLine) {
    return Optional.ofNullable(moveLine)
        .map(MoveLine::getMove)
        .map(Move::getCompany)
        .map(Company::getCurrency)
        .orElse(null);
  }

  protected BigDecimal getCurrencyAmount(
      MoveLine moveLine,
      Object importedCurrency,
      Object importedCurrencyAmount,
      String entryNumber,
      FECImport fecImport)
      throws AxelorException {
    BigDecimal absoluteCurrencyAmount =
        getAbsoluteCurrencyAmount(
            moveLine, importedCurrency, importedCurrencyAmount, entryNumber, fecImport);

    return isDebitLine(moveLine) ? absoluteCurrencyAmount : absoluteCurrencyAmount.negate();
  }

  protected BigDecimal getAbsoluteCurrencyAmount(
      MoveLine moveLine,
      Object importedCurrency,
      Object importedCurrencyAmount,
      String entryNumber,
      FECImport fecImport)
      throws AxelorException {
    BigDecimal currencyAmount = parseCurrencyAmount(importedCurrencyAmount);
    if (currencyAmount != null && currencyAmount.signum() != 0) {
      return currencyAmount.abs();
    }

    if (isCompanyCurrency(importedCurrency, getCompanyCurrency(moveLine))) {
      return getAccountingAmount(moveLine).abs();
    }

    throw new AxelorException(
        fecImport,
        TraceBackRepository.CATEGORY_MISSING_FIELD,
        I18n.get(AccountExceptionMessage.IMPORT_FEC_FOREIGN_CURRENCY_AMOUNT_REQUIRED),
        entryNumber);
  }

  protected BigDecimal parseCurrencyAmount(Object importedCurrencyAmount) {
    if (ObjectUtils.isEmpty(importedCurrencyAmount)
        || StringUtils.isBlank(importedCurrencyAmount.toString())) {
      return null;
    }

    String currencyAmountValue = importedCurrencyAmount.toString().trim();
    return new BigDecimal(currencyAmountValue.replace(',', '.'));
  }

  protected void normalizeDebitCredit(MoveLine moveLine) {
    BigDecimal debit = moveLine.getDebit();
    BigDecimal credit = moveLine.getCredit();

    if (debit.signum() < 0 && credit.signum() == 0) {
      moveLine.setDebit(BigDecimal.ZERO);
      moveLine.setCredit(debit.abs());
      return;
    }

    if (credit.signum() < 0 && debit.signum() == 0) {
      moveLine.setCredit(BigDecimal.ZERO);
      moveLine.setDebit(credit.abs());
    }
  }

  protected BigDecimal getAccountingAmount(MoveLine moveLine) {
    return isDebitLine(moveLine) ? moveLine.getDebit() : moveLine.getCredit();
  }

  protected boolean isDebitLine(MoveLine moveLine) {
    return moveLine.getDebit().signum() != 0;
  }
}
