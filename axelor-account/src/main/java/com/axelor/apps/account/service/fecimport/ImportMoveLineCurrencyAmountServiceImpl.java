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

public class ImportMoveLineCurrencyAmountServiceImpl
    implements ImportMoveLineCurrencyAmountService {

  @Override
  public void computeImportedCurrencyAmount(
      MoveLine moveLine,
      Object importedCurrency,
      Object importedCurrencyAmount,
      String entryNumber,
      FECImport fecImport)
      throws AxelorException {
    if (moveLine == null) {
      return;
    }

    if (isZero(moveLine.getDebit()) && isZero(moveLine.getCredit())) {
      moveLine.setCurrencyAmount(BigDecimal.ZERO);
      return;
    }

    BigDecimal currencyAmount =
        getCurrencyAmount(
            moveLine, importedCurrency, importedCurrencyAmount, entryNumber, fecImport);
    moveLine.setCurrencyAmount(applySign(moveLine, currencyAmount));
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
    BigDecimal currencyAmount = parseCurrencyAmount(importedCurrencyAmount);
    if (!isZero(currencyAmount)) {
      return currencyAmount.abs();
    }

    if (isCompanyCurrency(importedCurrency, getCompanyCurrency(moveLine))) {
      return getSourceAmount(moveLine).abs();
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

    String currencyAmount = importedCurrencyAmount.toString().trim();
    return new BigDecimal(currencyAmount.replace(',', '.'));
  }

  protected BigDecimal applySign(MoveLine moveLine, BigDecimal absoluteAmount) {
    BigDecimal amount = absoluteAmount.abs();

    if (!isZero(moveLine.getDebit())) {
      return moveLine.getDebit().signum() > 0 ? amount : amount.negate();
    }

    return moveLine.getCredit().signum() < 0 ? amount : amount.negate();
  }

  protected BigDecimal getSourceAmount(MoveLine moveLine) {
    return !isZero(moveLine.getDebit()) ? moveLine.getDebit() : moveLine.getCredit();
  }

  protected boolean isZero(BigDecimal amount) {
    return amount == null || amount.signum() == 0;
  }
}
