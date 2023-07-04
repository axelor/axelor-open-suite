package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.Contract;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.script.ScriptException;

public interface ContractRevaluationService {
  Contract applyFormula(Contract contract) throws AxelorException, ScriptException;

  boolean isToRevaluate(LocalDate todayDate, Contract contract);

  long getNumberOfContractsToRevaluateToday();

  void checkIfRevaluationIsNeeded(
      BigDecimal newYearlyExTotalRevalued,
      BigDecimal initialYearlyExTotalRevalued,
      Contract contract)
      throws AxelorException;
}
