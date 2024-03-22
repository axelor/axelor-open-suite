package com.axelor.apps.base.interfaces;

import com.axelor.apps.base.service.app.AppBaseService;
import java.math.BigDecimal;
import java.math.RoundingMode;

@FunctionalInterface
public interface ArithmeticOperation {
  BigDecimal operate(BigDecimal a, BigDecimal b);

  static BigDecimal operateDivide(BigDecimal a, BigDecimal b) {
    return getDivideArithmeticOperation().operate(a, b);
  }

  static ArithmeticOperation getDivideArithmeticOperation() {
    return (BigDecimal x, BigDecimal y) ->
        x.divide(y, AppBaseService.DEFAULT_EXCHANGE_RATE_REVERSION_SCALE, RoundingMode.HALF_UP)
            .setScale(AppBaseService.DEFAULT_EXCHANGE_RATE_SCALE, RoundingMode.HALF_UP);
  }
}
