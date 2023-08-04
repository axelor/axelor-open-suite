package com.axelor.apps.account.service;

import java.math.BigDecimal;

public interface ScaleServiceAccount {

  BigDecimal getScaledValue(Object object, BigDecimal amount, boolean isCompanyAmount);
}
