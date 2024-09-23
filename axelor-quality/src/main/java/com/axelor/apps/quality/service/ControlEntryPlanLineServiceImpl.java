/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
  protected ControlEntrySampleUpdateService controlEntrySampleUpdateService;

  @Inject
  public ControlEntryPlanLineServiceImpl(
      ControlEntryPlanLineRepository controlEntryPlanLineRepository,
      ControlEntrySampleRepository controlEntrySampleRepository,
      ControlEntrySampleUpdateService controlEntrySampleUpdateService) {
    this.controlEntryPlanLineRepository = controlEntryPlanLineRepository;
    this.controlEntrySampleRepository = controlEntrySampleRepository;
    this.controlEntrySampleUpdateService = controlEntrySampleUpdateService;
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

    Object result;

    try {
      result = scriptHelper.eval(formula);
    } catch (IllegalArgumentException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(QualityExceptionMessage.EVAL_FORMULA_NULL_FIELDS));
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
