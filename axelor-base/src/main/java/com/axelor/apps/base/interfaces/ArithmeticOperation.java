package com.axelor.apps.base.interfaces;

import java.math.BigDecimal;

@FunctionalInterface
public interface ArithmeticOperation {
  BigDecimal operate(BigDecimal a, BigDecimal b);
}
