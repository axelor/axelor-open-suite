/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.common.ObjectUtils;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineRecordServiceImpl implements MoveLineRecordService {
  protected AppAccountService appAccountService;
  protected MoveLoadDefaultConfigService moveLoadDefaultConfigService;
  protected FiscalPositionService fiscalPositionService;
  protected CurrencyService currencyService;
  protected CurrencyScaleService currencyScaleService;
  protected TaxService taxService;
  protected MoveLineToolService moveLineToolService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;

  @Inject
  public MoveLineRecordServiceImpl(
      AppAccountService appAccountService,
      MoveLoadDefaultConfigService moveLoadDefaultConfigService,
      FiscalPositionService fiscalPositionService,
      CurrencyService currencyService,
      CurrencyScaleService currencyScaleService,
      TaxService taxService,
      MoveLineToolService moveLineToolService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService) {
    this.appAccountService = appAccountService;
    this.moveLoadDefaultConfigService = moveLoadDefaultConfigService;
    this.fiscalPositionService = fiscalPositionService;
    this.currencyService = currencyService;
    this.currencyScaleService = currencyScaleService;
    this.taxService = taxService;
    this.moveLineToolService = moveLineToolService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
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

    moveLine.setCurrencyRate(currencyRate);

    BigDecimal total = moveLine.getCredit().add(moveLine.getDebit());

    if (total.signum() != 0) {
      boolean isCredit = moveLine.getCredit().signum() > 0;
      BigDecimal currencyAmount =
          total.divide(
              moveLine.getCurrencyRate(),
              currencyScaleService.getScale(move),
              RoundingMode.HALF_UP);
      if (isCredit) {
        currencyAmount = currencyAmount.negate();
      }
      moveLine.setCurrencyAmount(currencyAmount);
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
      moveLine.setTaxLineSet(Sets.newHashSet());
      moveLine.setTaxLineBeforeReverseSet(Sets.newHashSet());
      return;
    }

    Set<TaxLine> taxLineSet =
        moveLoadDefaultConfigService.getTaxLineSet(move, moveLine, accountingAccount);
    Set<TaxLine> taxLineBeforeReverseSet = moveLine.getTaxLineBeforeReverseSet();

    TaxEquiv taxEquiv = null;
    if (CollectionUtils.isNotEmpty(taxLineSet) && move.getFiscalPosition() != null) {
      taxEquiv =
          fiscalPositionService.getTaxEquivFromOrToTaxSet(
              move.getFiscalPosition(), taxLineBeforeReverseSet);
      if (taxEquiv != null) {
        moveLine.setTaxLineBeforeReverseSet(taxLineBeforeReverseSet);
        taxLineSet = taxService.getTaxLineSet(taxEquiv.getToTaxSet(), moveLine.getDate());
      }
    }
    moveLine.setTaxLineSet(taxLineSet);
    moveLine.setTaxEquiv(taxEquiv);

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
    BigDecimal currencyAmount = moveLine.getCurrencyAmount();
    BigDecimal currencyRate = moveLine.getCurrencyRate();
    BigDecimal newCurrencyAmount =
        currencyScaleService.getCompanyScaledValue(
            moveLine, currencyAmount.multiply(currencyRate).abs());

    if (currencyAmount.signum() < 0) {
      moveLine.setDebit(BigDecimal.ZERO);
      moveLine.setCredit(newCurrencyAmount);
    }

    if (currencyAmount.signum() > 0) {
      moveLine.setCredit(BigDecimal.ZERO);
      moveLine.setDebit(newCurrencyAmount);
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

  @Override
  public void setMoveLineDates(Move move) throws AxelorException {
    if (move.getDate() != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        moveLine.setDate(move.getDate());
        moveLineToolService.checkDateInPeriod(move, moveLine);
        this.computeDate(moveLine, move);
      }
    }
  }

  @Override
  public void setMoveLineOriginDates(Move move) throws AxelorException {
    if (move.getOriginDate() != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        moveLine.setOriginDate(move.getOriginDate());
      }
    }
  }

  @Override
  public void computeDate(MoveLine moveLine, Move move) throws AxelorException {
    if (move != null && move.getJournal() != null && move.getJournal().getIsFillOriginDate()) {
      this.setOriginDate(moveLine);
    }
    moveLineComputeAnalyticService.computeAnalyticDistribution(moveLine, move);
    if (move != null && move.getMassEntryStatusSelect() == MoveRepository.MASS_ENTRY_STATUS_NULL) {
      moveLineToolService.checkDateInPeriod(move, moveLine);
    }
  }
}
