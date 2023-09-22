package com.axelor.apps.account.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Year;
import java.time.LocalDate;

public interface YearAccountService {
  Year generateFiscalYear(
      Company company,
      LocalDate fromDate,
      LocalDate toDate,
      Integer periodDuration,
      LocalDate reportedBalancedDate)
      throws AxelorException;

  Year createYear(
      Company company,
      String name,
      String code,
      LocalDate fromDate,
      LocalDate toDate,
      Integer periodDuration,
      int typeSelect,
      LocalDate reportedBalanceDate);
}
