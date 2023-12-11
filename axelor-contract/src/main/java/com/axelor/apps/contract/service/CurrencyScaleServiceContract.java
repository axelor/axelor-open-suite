package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import java.math.BigDecimal;

public interface CurrencyScaleServiceContract {

  BigDecimal getScaledValue(Contract contract, BigDecimal amount);

  BigDecimal getCompanyScaledValue(Contract contract, BigDecimal amount);

  BigDecimal getScaledValue(ContractLine contractLine, BigDecimal amount);

  BigDecimal getCompanyScaledValue(ContractLine contractLine, BigDecimal amount);

  int getScale(Contract contract);

  int getCompanyScale(Company company);

  int getScale(Currency currency);
}
