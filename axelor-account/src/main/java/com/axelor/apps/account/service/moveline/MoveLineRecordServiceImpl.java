package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoveLineRecordServiceImpl implements MoveLineRecordService {
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
}
