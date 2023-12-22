package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.db.ControlType;
import com.axelor.apps.quality.db.repo.ControlEntryPlanLineRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import java.util.Objects;
import java.util.Optional;

public class ControlEntryPlanLineServiceImpl implements ControlEntryPlanLineService {
  @Override
  public void conformityEval(ControlEntryPlanLine controlEntryPlanLine) throws AxelorException {

    Objects.requireNonNull(controlEntryPlanLine);

    if (controlEntryPlanLine.getTypeSelect()
        == ControlEntryPlanLineRepository.TYPE_ENTRY_SAMPLE_LINE) {
      String formula = this.getFormula(controlEntryPlanLine);

      Context scriptContext =
          new Context(Mapper.toMap(controlEntryPlanLine), ControlEntryPlanLine.class);
      ScriptHelper scriptHelper = new GroovyScriptHelper(scriptContext);

      Object result = scriptHelper.eval(formula);

      if (!(result instanceof Integer)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(QualityExceptionMessage.EXPECTED_INT_RESULT_FORMULA),
            result);
      }

      controlEntryPlanLine.setResultSelect((Integer) result);
    }
  }

  @Override
  public String getFormula(ControlEntryPlanLine controlEntryPlanLine) throws AxelorException {
    Objects.requireNonNull(controlEntryPlanLine);

    if (controlEntryPlanLine.getTypeSelect() == ControlEntryPlanLineRepository.TYPE_PLAN_LINE) {
      return Optional.ofNullable(controlEntryPlanLine.getControlType())
          .map(ControlType::getConformityFormula)
          .orElseThrow(
              () ->
                  new AxelorException(
                      TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                      I18n.get(QualityExceptionMessage.CAN_NOT_FETCH_FORMULA)));
    }
    // Type entry
    return getFormula(controlEntryPlanLine.getControlPlanLine());
  }
}
