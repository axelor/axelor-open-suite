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
package com.axelor.apps.quality.rest.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.CharacteristicProperty;
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.db.ControlTypeField;
import com.axelor.apps.quality.db.ControlTypeFieldValue;
import com.axelor.apps.quality.db.repo.ControlEntryPlanLineRepository;
import com.axelor.apps.quality.db.repo.ControlTypeFieldRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.quality.rest.dto.ControlEntrySampleLinePutRequest;
import com.axelor.apps.quality.rest.dto.ControlTypeFieldValuePutRequest;
import com.axelor.apps.quality.service.ControlEntryPlanLineService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.utils.api.ConflictChecker;
import com.axelor.utils.api.ObjectFinder;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ControlEntrySampleLineUpdateAPIServiceImpl
    implements ControlEntrySampleLineUpdateAPIService {

  protected static final String DECIMAL_VALUE = "decimalValue";
  protected static final String TEXT_VALUE = "textValue";
  protected static final String BOOLEAN_VALUE = "booleanValue";
  protected static final String SELECTION_VALUE = "selectionValue";

  protected ControlEntryPlanLineService controlEntryPlanLineService;
  protected ControlEntryPlanLineRepository controlEntryPlanLineRepository;

  @Inject
  public ControlEntrySampleLineUpdateAPIServiceImpl(
      ControlEntryPlanLineService controlEntryPlanLineService,
      ControlEntryPlanLineRepository controlEntryPlanLineRepository) {
    this.controlEntryPlanLineService = controlEntryPlanLineService;
    this.controlEntryPlanLineRepository = controlEntryPlanLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ControlEntryPlanLine updateValuesAndCheckConformity(
      ControlEntryPlanLine controlEntrySampleLine, ControlEntrySampleLinePutRequest request)
      throws AxelorException {

    checkSampleLine(controlEntrySampleLine);

    Map<Long, ControlTypeFieldValue> valueMap =
        Optional.ofNullable(controlEntrySampleLine.getEntryValueList())
            .orElseGet(ArrayList::new)
            .stream()
            .collect(Collectors.toMap(ControlTypeFieldValue::getId, Function.identity()));

    for (ControlTypeFieldValuePutRequest valueRequest : request.getEntryValueList()) {
      ControlTypeFieldValue value = valueMap.get(valueRequest.getId());
      if (value == null) {
        throw new AxelorException(
            controlEntrySampleLine,
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(QualityExceptionMessage.API_ENTRY_VALUE_NOT_IN_SAMPLE_LINE),
            valueRequest.getId(),
            controlEntrySampleLine.getId());
      }
      ConflictChecker.checkVersion(value, valueRequest.getVersion());
      setValue(value, valueRequest);
    }

    // the formula reads the values from the database: the pending changes are written first
    JPA.flush();

    controlEntrySampleLine.setResultSelect(
        controlEntryPlanLineService.evaluateResult(controlEntrySampleLine));

    // the repository propagates the result of the line to its sample
    return controlEntryPlanLineRepository.save(controlEntrySampleLine);
  }

  protected void checkSampleLine(ControlEntryPlanLine controlEntrySampleLine)
      throws AxelorException {
    if (ControlEntryPlanLineRepository.TYPE_ENTRY_SAMPLE_LINE
        != controlEntrySampleLine.getTypeSelect()) {
      throw new AxelorException(
          controlEntrySampleLine,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(QualityExceptionMessage.API_NOT_A_CONTROL_ENTRY_SAMPLE_LINE),
          controlEntrySampleLine.getId());
    }
  }

  /** Writes the value in the field matching the type of the control type field. */
  protected void setValue(ControlTypeFieldValue value, ControlTypeFieldValuePutRequest valueRequest)
      throws AxelorException {

    ControlTypeField field = value.getControlTypeField();
    checkValueType(field, valueRequest);

    switch (field.getTypeSelect()) {
      case ControlTypeFieldRepository.TYPE_DECIMAL:
        value.setDecimalValue(valueRequest.getDecimalValue());
        break;
      case ControlTypeFieldRepository.TYPE_BOOLEAN:
        value.setBooleanValue(valueRequest.getBooleanValue());
        break;
      case ControlTypeFieldRepository.TYPE_SELECTION:
        value.setSelectionValue(fetchSelectionValue(field, valueRequest.getSelectionValue()));
        break;
      default:
        value.setTextValue(valueRequest.getTextValue());
    }
  }

  /**
   * A value sent in a field of another type than the one of the control type field would be
   * silently lost: it is rejected instead.
   */
  protected void checkValueType(
      ControlTypeField field, ControlTypeFieldValuePutRequest valueRequest) throws AxelorException {

    String expectedValueField = getValueFieldName(field.getTypeSelect());

    List<String> otherFilledValueFields =
        getFilledValueFieldNames(valueRequest).stream()
            .filter(name -> !Objects.equals(name, expectedValueField))
            .collect(Collectors.toList());

    if (!otherFilledValueFields.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(QualityExceptionMessage.API_ENTRY_VALUE_TYPE_MISMATCH),
          field.getName(),
          String.join(", ", otherFilledValueFields),
          expectedValueField);
    }
  }

  protected String getValueFieldName(int typeSelect) {
    switch (typeSelect) {
      case ControlTypeFieldRepository.TYPE_DECIMAL:
        return DECIMAL_VALUE;
      case ControlTypeFieldRepository.TYPE_BOOLEAN:
        return BOOLEAN_VALUE;
      case ControlTypeFieldRepository.TYPE_SELECTION:
        return SELECTION_VALUE;
      default:
        return TEXT_VALUE;
    }
  }

  protected List<String> getFilledValueFieldNames(ControlTypeFieldValuePutRequest valueRequest) {
    List<String> names = new ArrayList<>();
    if (valueRequest.getDecimalValue() != null) {
      names.add(DECIMAL_VALUE);
    }
    if (valueRequest.getTextValue() != null) {
      names.add(TEXT_VALUE);
    }
    if (valueRequest.getBooleanValue() != null) {
      names.add(BOOLEAN_VALUE);
    }
    if (valueRequest.getSelectionValue() != null) {
      names.add(SELECTION_VALUE);
    }
    return names;
  }

  /** The selection is restricted to the allowed values of the field, as in the form editor. */
  protected CharacteristicProperty fetchSelectionValue(
      ControlTypeField field, Long selectionValueId) throws AxelorException {

    if (selectionValueId == null) {
      return null;
    }

    CharacteristicProperty selectionValue =
        ObjectFinder.find(CharacteristicProperty.class, selectionValueId, ObjectFinder.NO_VERSION);

    if (field.getValueSet() == null || !field.getValueSet().contains(selectionValue)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(QualityExceptionMessage.API_SELECTION_VALUE_NOT_ALLOWED),
          selectionValue.getName(),
          field.getName());
    }
    return selectionValue;
  }
}
