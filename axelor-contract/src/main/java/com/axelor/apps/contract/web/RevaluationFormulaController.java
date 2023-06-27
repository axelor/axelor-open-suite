package com.axelor.apps.contract.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.RevaluationFormula;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.apps.contract.service.RevaluationFormulaService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import javax.script.ScriptException;

public class RevaluationFormulaController {
  public void checkFormula(ActionRequest request, ActionResponse response) {
    try {
      RevaluationFormula revaluationFormula = request.getContext().asType(RevaluationFormula.class);
      Beans.get(RevaluationFormulaService.class).checkFormula(revaluationFormula);
    } catch (ScriptException e) {
      response.setInfo(I18n.get(ContractExceptionMessage.CONTRACT_FORMULA_ERROR_IN_FORMULA));
      response.addError(
          "formula",
          I18n.get(ContractExceptionMessage.CONTRACT_FORMULA_ERROR_IN_FORMULA) + e.getMessage());
      TraceBackService.trace(response, e);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
