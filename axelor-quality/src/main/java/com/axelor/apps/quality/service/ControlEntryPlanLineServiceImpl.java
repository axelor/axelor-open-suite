package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.db.ControlType;
import com.axelor.apps.quality.db.repo.ControlEntryPlanLineRepository;
import com.axelor.apps.quality.db.repo.ControlEntrySampleRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;
import java.util.Optional;

public class ControlEntryPlanLineServiceImpl implements ControlEntryPlanLineService {

  protected ControlEntryPlanLineRepository controlEntryPlanLineRepository;
  protected ControlEntrySampleRepository controlEntrySampleRepository;

  @Inject
  public ControlEntryPlanLineServiceImpl(
      ControlEntryPlanLineRepository controlEntryPlanLineRepository,
      ControlEntrySampleRepository controlEntrySampleRepository) {
    this.controlEntryPlanLineRepository = controlEntryPlanLineRepository;
    this.controlEntrySampleRepository = controlEntrySampleRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void conformityEval(ControlEntryPlanLine controlEntryPlanLine) throws AxelorException {

    Objects.requireNonNull(controlEntryPlanLine);

    if (ControlEntryPlanLineRepository.TYPE_ENTRY_SAMPLE_LINE
        == controlEntryPlanLine.getTypeSelect()) {
      eval(controlEntryPlanLine);
    }
  }

  protected void eval(ControlEntryPlanLine controlEntryPlanLine) throws AxelorException {
    String formula = this.getFormula(controlEntryPlanLine);

    Context scriptContext =
        new Context(Mapper.toMap(controlEntryPlanLine), ControlEntryPlanLine.class);
    ScriptHelper scriptHelper = new GroovyScriptHelper(scriptContext);

    Object result = scriptHelper.eval(formula);

    if (!(result instanceof Boolean)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(QualityExceptionMessage.EXPECTED_BOOLEAN_RESULT_FORMULA),
          result);
    }

    boolean isCompliant = (boolean) result;
    if (isCompliant) {
      controlEntryPlanLine.setResultSelect(ControlEntryPlanLineRepository.RESULT_COMPLIANT);
    } else {
      controlEntryPlanLine.setResultSelect(ControlEntryPlanLineRepository.RESULT_NOT_COMPLIANT);
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

  @Override
  public ControlEntryPlanLine createEntryWithPlan(ControlEntryPlanLine controlEntryPlanLine) {

    Objects.requireNonNull(controlEntryPlanLine);

    if (ControlEntryPlanLineRepository.TYPE_PLAN_LINE != controlEntryPlanLine.getTypeSelect()) {
      return null;
    }
    ControlEntryPlanLine res = controlEntryPlanLineRepository.copy(controlEntryPlanLine, false);
    res.setControlPlanLine(controlEntryPlanLine);
    res.setControlPlan(null);
    res.setResultSelect(ControlEntryPlanLineRepository.RESULT_NOT_CONTROLLED);
    res.setPlanAttrs(controlEntryPlanLine.getPlanAttrs());
    res.setTypeSelect(ControlEntryPlanLineRepository.TYPE_ENTRY_SAMPLE_LINE);
    res.setEntryAttrs(controlEntryPlanLine.getEntryAttrs());

    return res;
  }
}
