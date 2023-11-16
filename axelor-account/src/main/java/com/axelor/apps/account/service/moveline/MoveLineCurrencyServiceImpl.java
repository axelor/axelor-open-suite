/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.service.CurrencyScaleServiceAccount;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class MoveLineCurrencyServiceImpl implements MoveLineCurrencyService {

  protected CurrencyService currencyService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected MoveLineService moveLineService;
  protected MoveToolService moveToolService;
  protected CurrencyScaleServiceAccount currencyScaleServiceAccount;

  @Inject
  public MoveLineCurrencyServiceImpl(
      CurrencyService currencyService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      MoveLineService moveLineService,
      MoveToolService moveToolService,
      CurrencyScaleServiceAccount currencyScaleServiceAccount) {
    this.currencyService = currencyService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.moveLineService = moveLineService;
    this.moveToolService = moveToolService;
    this.currencyScaleServiceAccount = currencyScaleServiceAccount;
  }

  @Override
  public void computeNewCurrencyRateOnMoveLineList(Move move, LocalDate dueDate)
      throws AxelorException {
    BigDecimal currencyRate =
        currencyService.getCurrencyConversionRate(
            move.getCurrency(), move.getCompanyCurrency(), move.getDate());

    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getDebit().add(moveLine.getCredit()).signum() == 0) {
        if (moveLine.getAccount() != null) {
          BigDecimal computedAmount = moveLine.getCurrencyAmount().multiply(currencyRate);
          switch (moveLine.getAccount().getCommonPosition()) {
            case AccountRepository.COMMON_POSITION_CREDIT:
              moveLine.setCredit(computedAmount);
              break;
            case AccountRepository.COMMON_POSITION_DEBIT:
              moveLine.setDebit(computedAmount);
              break;
            default:
              break;
          }
        }
      }
      BigDecimal currencyAmount = moveLine.getDebit().add(moveLine.getCredit());
      currencyAmount =
          currencyAmount.divide(
              currencyRate, currencyScaleServiceAccount.getScale(move), RoundingMode.HALF_UP);

      moveLine.setCurrencyAmount(
          moveToolService.computeCurrencyAmountSign(
              currencyAmount, moveLine.getDebit().signum() > 0));
      moveLine.setCurrencyRate(currencyRate);

      moveLine.clearInvoiceTermList();
      moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, dueDate, false);
      moveLineService.computeFinancialDiscount(moveLine);
    }
  }
}
