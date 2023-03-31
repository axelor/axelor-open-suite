package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class MoveLineRecordServiceImpl implements MoveLineRecordService {
  protected AppAccountService appAccountService;
  protected MoveLoadDefaultConfigService moveLoadDefaultConfigService;
  protected FiscalPositionService fiscalPositionService;

  @Inject
  public MoveLineRecordServiceImpl(
      AppAccountService appAccountService,
      MoveLoadDefaultConfigService moveLoadDefaultConfigService,
      FiscalPositionService fiscalPositionService) {
    this.appAccountService = appAccountService;
    this.moveLoadDefaultConfigService = moveLoadDefaultConfigService;
    this.fiscalPositionService = fiscalPositionService;
  }

  @Override
  public void setCurrencyFields(MoveLine moveLine, Move move) throws AxelorException {
    Currency currency = move.getCurrency();
    Currency companyCurrency = move.getCompanyCurrency();
    BigDecimal currencyRate = BigDecimal.ONE;

    if (currency != null && companyCurrency != null && !currency.equals(companyCurrency)) {
      if (move.getMoveLineList().size() == 0) {
        currencyRate =
            Beans.get(CurrencyService.class).getCurrencyConversionRate(currency, companyCurrency);
      } else {
        currencyRate = move.getMoveLineList().get(0).getCurrencyRate();
      }
    }

    moveLine.setCurrencyRate(currencyRate);

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
        || !moveLine.getAccount().getManageCutOffPeriod()) {
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
      return;
    }

    TaxLine taxLine = moveLoadDefaultConfigService.getTaxLine(move, moveLine, accountingAccount);
    TaxEquiv taxEquiv = null;

    if (taxLine != null) {
      if (move.getFiscalPosition() != null) {
        taxEquiv = fiscalPositionService.getTaxEquiv(move.getFiscalPosition(), taxLine.getTax());
      }

      moveLine.setTaxLine(taxLine);

      if (taxEquiv != null) {
        moveLine.setTaxEquiv(taxEquiv);
      }
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
}
