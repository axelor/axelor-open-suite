package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.Contract;
import java.math.BigDecimal;

public interface CurrencyScaleServiceContract {

  BigDecimal getScaledValue();

  int getScale(Contract contract);
}
