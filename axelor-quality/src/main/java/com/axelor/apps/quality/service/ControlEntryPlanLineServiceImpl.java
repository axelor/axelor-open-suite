/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.db.ControlType;
import com.axelor.apps.quality.db.ControlTypeFieldValue;
import com.axelor.apps.quality.db.repo.ControlEntryPlanLineRepository;
import com.axelor.apps.quality.db.repo.ControlEntrySampleRepository;
import com.axelor.apps.quality.db.repo.ControlTypeFieldValueRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.axelor.script.ScriptHelper;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ControlEntryPlanLineServiceImpl implements ControlEntryPlanLineService {

  protected ControlEntryPlanLineRepository controlEntryPlanLineRepository;
  protected ControlEntrySampleRepository controlEntrySampleRepository;
  protected ControlEntrySampleUpdateService controlEntrySampleUpdateService;
  protected ControlTypeFieldValueService controlTypeFieldValueService;
  protected ControlTypeFieldValueRepository controlTypeFieldValueRepository;

  @Inject
  public ControlEntryPlanLineServiceImpl(
      ControlEntryPlanLineRepository controlEntryPlanLineRepository,
      ControlEntrySampleRepository controlEntrySampleRepository,
      ControlEntrySampleUpdateService controlEntrySampleUpdateService,
      ControlTypeFieldValueService controlTypeFieldValueService,
      ControlTypeFieldValueRepository controlTypeFieldValueRepository) {
    this.controlEntryPlanLineRepository = controlEntryPlanLineRepository;
    this.controlEntrySampleRepository = controlEntrySampleRepository;
    this.controlEntrySampleUpdateService = controlEntrySampleUpdateService;
    this.controlTypeFieldValueService = controlTypeFieldValueService;
    this.controlTypeFieldValueRepository = controlTypeFieldValueRepository;
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

    ScriptHelper scriptHelper = new GroovyScriptHelper(getBindings(controlEntryPlanLine));

    Object result;

    try {
      result = scriptHelper.eval(formula);
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(QualityExceptionMessage.EVAL_FORMULA_ERROR),
          e.getMessage());
    }

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

  /**
   * Bindings of the conformity formula: the reference values of the control plan line under {@code
   * plan}, the values measured on the sample line under {@code entry}, both indexed by field code.
   * Reference values are read from the control plan line, they are not duplicated on entry lines.
   */
  protected ScriptBindings getBindings(ControlEntryPlanLine controlEntryPlanLine)
      throws AxelorException {

    ControlEntryPlanLine controlPlanLine = controlEntryPlanLine.getControlPlanLine();
    List<ControlTypeFieldValue> values = fetchValues(controlPlanLine, controlEntryPlanLine);

    List<ControlTypeFieldValue> planValues =
        values.stream().filter(value -> value.getPlanLine() != null).collect(Collectors.toList());
    List<ControlTypeFieldValue> entryValues =
        values.stream().filter(value -> value.getEntryLine() != null).collect(Collectors.toList());

    controlTypeFieldValueService.checkRequiredValues(
        Optional.ofNullable(controlPlanLine)
            .map(ControlEntryPlanLine::getControlType)
            .orElseGet(controlEntryPlanLine::getControlType),
        planValues,
        entryValues);

    Map<String, Object> bindings = new HashMap<>();
    bindings.put("plan", controlTypeFieldValueService.toScriptMap(planValues));
    bindings.put("entry", controlTypeFieldValueService.toScriptMap(entryValues));
    bindings.put("line", controlEntryPlanLine);

    return new ScriptBindings(bindings);
  }

  /** Reference and measured values of the couple (control plan line, entry line), in one query. */
  protected List<ControlTypeFieldValue> fetchValues(
      ControlEntryPlanLine controlPlanLine, ControlEntryPlanLine entryLine) {
    return controlTypeFieldValueRepository
        .all()
        .filter("self.planLine = :planLine OR self.entryLine = :entryLine")
        .bind("planLine", controlPlanLine)
        .bind("entryLine", entryLine)
        .fetch();
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
    res.setTypeSelect(ControlEntryPlanLineRepository.TYPE_ENTRY_SAMPLE_LINE);

    // reference values are read from the control plan line, only the measured values are created
    controlTypeFieldValueService.createEntryValues(res, controlEntryPlanLine.getControlType());

    return res;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void conformityEvalWithUpdate(ControlEntryPlanLine controlEntryPlanLine)
      throws AxelorException {

    Objects.requireNonNull(controlEntryPlanLine);

    this.conformityEval(controlEntryPlanLine);

    if (ControlEntryPlanLineRepository.TYPE_ENTRY_SAMPLE_LINE
        == controlEntryPlanLine.getTypeSelect()) {

      if (controlEntryPlanLine.getControlEntrySample() != null) {
        controlEntrySampleUpdateService.updateResult(controlEntryPlanLine.getControlEntrySample());
      }
    }
  }
}
