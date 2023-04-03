package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class MoveLineCurrencyServiceImpl implements MoveLineCurrencyService {

  protected CurrencyService currencyService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected MoveLineService moveLineService;

  @Inject
  public MoveLineCurrencyServiceImpl(
      CurrencyService currencyService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      MoveLineService moveLineService) {
    this.currencyService = currencyService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.moveLineService = moveLineService;
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
              currencyRate, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);

      moveLine.setCurrencyAmount(currencyAmount);
      moveLine.setCurrencyRate(currencyRate);

      moveLine.clearInvoiceTermList();
      moveLineInvoiceTermService.generateDefaultInvoiceTerm(moveLine, dueDate, false);
      moveLineService.computeFinancialDiscount(moveLine);
    }
  }
}
