package com.axelor.apps.dmn.service;

import com.axelor.db.Model;
import com.axelor.exception.AxelorException;

public interface DmnService {

  public void executeDmn(String decisionDefinitionId, Model model) throws AxelorException;

  public String createOutputToFieldScript(
      String decisionDefinitionId,
      String modelName,
      String searchOperator,
      String ifMultiple,
      String resultVar);
}
