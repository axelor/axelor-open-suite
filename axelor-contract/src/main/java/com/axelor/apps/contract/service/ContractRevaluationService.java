package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.Contract;
import javax.script.ScriptException;

public interface ContractRevaluationService {
  Contract applyFormula(Contract contract) throws AxelorException, ScriptException;
}
