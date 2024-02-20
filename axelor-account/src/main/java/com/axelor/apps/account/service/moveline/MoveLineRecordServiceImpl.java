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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;

public class MoveLineRecordServiceImpl implements MoveLineRecordService {
  protected AppAccountService appAccountService;
  protected MoveLoadDefaultConfigService moveLoadDefaultConfigService;
  protected FiscalPositionService fiscalPositionService;
  protected CurrencyService currencyService;

  @Inject
  public MoveLineRecordServiceImpl(
      AppAccountService appAccountService,
      MoveLoadDefaultConfigService moveLoadDefaultConfigService,
      FiscalPositionService fiscalPositionService,
      CurrencyService currencyService) {
    this.appAccountService = appAccountService;
    this.moveLoadDefaultConfigService = moveLoadDefaultConfigService;
    this.fiscalPositionService = fiscalPositionService;
    this.currencyService = currencyService;
  }

  @Override
  public void setCurrencyFields(MoveLine moveLine, Move move) throws AxelorException {
    Currency currency = move.getCurrency();
    Currency companyCurrency = move.getCompanyCurrency();
    BigDecimal currencyRate = BigDecimal.ONE;

    if (currency != null && companyCurrency != null && !currency.equals(companyCurrency)) {
      if (ObjectUtils.isEmpty(move.getMoveLineList())) {
        currencyRate =
            currencyService.getCurrencyConversionRate(currency, companyCurrency, move.getDate());
      } else {
        currencyRate = move.getMoveLineList().get(0).getCurrencyRate();
      }
    }

    moveLine.setCurrencyRate(currencyRate.setScale(5, RoundingMode.HALF_UP));

    BigDecimal total = moveLine.getCredit().add(moveLine.getDebit());

    if (total.signum() != 0) {
      moveLine.setCurrencyAmount(
          total.divide(
              moveLine.getCurrencyRate(),
              AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
              RoundingMode.HALF_UP));
    }
  }

  @Override
  public void setCutOffDates(
      MoveLine moveLine, LocalDate cutOffStartDate, LocalDate cutOffEndDate) {
    if (!appAccountService.getAppAccount().getManageCutOffPeriod()
        || (ObjectUtils.notEmpty(moveLine.getAccount())
            && !moveLine.getAccount().getManageCutOffPeriod())) {
      return;
    }

    moveLine.setCutOffStartDate(cutOffStartDate);
    moveLine.setCutOffEndDate(cutOffEndDate);
  }

  @Override
  public void setIsCutOffGeneratedFalse(MoveLine moveLine) {
    moveLine.setIsCutOffGenerated(false);
  }

  @Override
  public void refreshAccountInformation(MoveLine moveLine, Move move) throws AxelorException {
    Account accountingAccount = moveLine.getAccount();

    if (accountingAccount == null || !accountingAccount.getIsTaxAuthorizedOnMoveLine()) {
      moveLine.setTaxLine(null);
      return;
    }

    TaxLine taxLine = moveLoadDefaultConfigService.getTaxLine(move, moveLine, accountingAccount);
    TaxEquiv taxEquiv = null;
    moveLine.setTaxLineBeforeReverse(null);
    if (taxLine != null) {
      if (move.getFiscalPosition() != null) {
        taxEquiv = fiscalPositionService.getTaxEquiv(move.getFiscalPosition(), taxLine.getTax());
      }

      moveLine.setTaxLine(taxLine);

      if (taxEquiv != null) {
        moveLine.setTaxEquiv(taxEquiv);
        moveLine.setTaxLineBeforeReverse(taxLine);
      }
    }

    if (ObjectUtils.notEmpty(accountingAccount.getVatSystemSelect())) {
      moveLine.setVatSystemSelect(accountingAccount.getVatSystemSelect());
    }
  }

  @Override
  public void setParentFromMove(MoveLine moveLine, Move move) {
    if (move != null && move.getPartner() != null) {
      moveLine.setPartner(move.getPartner());
    }
  }

  @Override
  public void setOriginDate(MoveLine moveLine) {
    moveLine.setOriginDate(moveLine.getDate());
  }

  @Override
  public void setDebitCredit(MoveLine moveLine) {
    if (moveLine.getAccount() == null) {
      return;
    }

    BigDecimal amount = moveLine.getCurrencyAmount().multiply(moveLine.getCurrencyRate());

    if (moveLine.getAccount().getCommonPosition() == AccountRepository.COMMON_POSITION_CREDIT) {
      moveLine.setCredit(amount);
    } else if (moveLine.getAccount().getCommonPosition()
        == AccountRepository.COMMON_POSITION_DEBIT) {
      moveLine.setDebit(amount);
    }
  }

  @Override
  public void resetCredit(MoveLine moveLine) {
    if (moveLine.getCredit().signum() != 0 && moveLine.getDebit().signum() != 0) {
      moveLine.setCredit(BigDecimal.ZERO);
    }
  }

  @Override
  public void resetDebit(MoveLine moveLine) {
    if (moveLine.getCredit().signum() != 0 && moveLine.getDebit().signum() != 0) {
      moveLine.setDebit(BigDecimal.ZERO);
    }
  }

  @Override
  public void resetPartnerFields(MoveLine moveLine) {
    if (moveLine.getPartner() == null) {
      moveLine.setPartnerId(null);
      moveLine.setPartnerSeq(null);
      moveLine.setPartnerFullName(null);
    }
  }

  @Override
  public void setCounter(MoveLine moveLine, Move move) {
    int counter =
        ObjectUtils.notEmpty(move.getMoveLineList())
            ? move.getMoveLineList().stream()
                .map(MoveLine::getCounter)
                .max(Comparator.naturalOrder())
                .orElse(0)
            : 0;
    moveLine.setCounter(counter + 1);
  }
}
