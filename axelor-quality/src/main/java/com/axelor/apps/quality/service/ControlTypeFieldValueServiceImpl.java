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
import com.axelor.apps.quality.db.ControlTypeField;
import com.axelor.apps.quality.db.ControlTypeFieldLine;
import com.axelor.apps.quality.db.ControlTypeFieldValue;
import com.axelor.apps.quality.db.repo.ControlTypeFieldRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.i18n.I18n;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ControlTypeFieldValueServiceImpl implements ControlTypeFieldValueService {

  protected static final Comparator<ControlTypeFieldLine> LINE_COMPARATOR =
      Comparator.comparingInt(
              (ControlTypeFieldLine line) -> Optional.ofNullable(line.getSequence()).orElse(0))
          .thenComparing(
              line ->
                  Optional.ofNullable(line.getControlTypeField())
                      .map(ControlTypeField::getName)
                      .orElse(""),
              String.CASE_INSENSITIVE_ORDER);

  @Override
  public List<ControlTypeFieldLine> getPlanFieldLines(ControlType controlType) {
    return sort(controlType == null ? null : controlType.getPlanFieldLineList());
  }

  @Override
  public List<ControlTypeFieldLine> getEntryFieldLines(ControlType controlType) {
    return sort(controlType == null ? null : controlType.getEntryFieldLineList());
  }

  protected List<ControlTypeFieldLine> sort(List<ControlTypeFieldLine> lines) {
    if (lines == null) {
      return Collections.emptyList();
    }
    return lines.stream()
        .filter(line -> line.getControlTypeField() != null)
        .sorted(LINE_COMPARATOR)
        .collect(Collectors.toList());
  }

  @Override
  public List<ControlTypeFieldValue> syncPlanValues(
      ControlType controlType, List<ControlTypeFieldValue> currentValues) {

    Map<ControlTypeField, ControlTypeFieldValue> currentValueMap = indexByField(currentValues);

    List<ControlTypeFieldValue> values = new ArrayList<>();
    for (ControlTypeFieldLine line : getPlanFieldLines(controlType)) {
      ControlTypeFieldValue value =
          Optional.ofNullable(currentValueMap.get(line.getControlTypeField()))
              .orElseGet(() -> createValue(line));
      copyFieldDefinition(value, line);
      values.add(value);
    }
    return values;
  }

  @Override
  public void createEntryValues(ControlEntryPlanLine entryLine, ControlType controlType) {
    Objects.requireNonNull(entryLine);

    getEntryFieldLines(controlType).stream()
        .map(this::createValue)
        .forEach(entryLine::addEntryValueListItem);
  }

  @Override
  public ControlTypeFieldValue createValue(ControlTypeFieldLine controlTypeFieldLine) {
    ControlTypeFieldValue value = new ControlTypeFieldValue();
    value.setControlTypeField(controlTypeFieldLine.getControlTypeField());
    copyFieldDefinition(value, controlTypeFieldLine);
    return value;
  }

  /**
   * The sequence and the type are copied on the value: an editor cannot rely on a dotted field of
   * the control type field, and a collection order by cannot cross that association either.
   */
  protected void copyFieldDefinition(ControlTypeFieldValue value, ControlTypeFieldLine line) {
    value.setSequence(Optional.ofNullable(line.getSequence()).orElse(0));
    value.setFieldTypeSelect(
        Optional.ofNullable(line.getControlTypeField())
            .map(ControlTypeField::getTypeSelect)
            .orElse(null));
  }

  @Override
  public ControlTypeFieldValue copyValue(ControlTypeFieldValue controlTypeFieldValue) {
    Objects.requireNonNull(controlTypeFieldValue);

    ControlTypeFieldValue copy = new ControlTypeFieldValue();
    copy.setControlTypeField(controlTypeFieldValue.getControlTypeField());
    copy.setSequence(controlTypeFieldValue.getSequence());
    copy.setFieldTypeSelect(controlTypeFieldValue.getFieldTypeSelect());
    copy.setDecimalValue(controlTypeFieldValue.getDecimalValue());
    copy.setTextValue(controlTypeFieldValue.getTextValue());
    copy.setBooleanValue(controlTypeFieldValue.getBooleanValue());
    copy.setSelectionValue(controlTypeFieldValue.getSelectionValue());
    return copy;
  }

  @Override
  public Map<String, Object> toScriptMap(Collection<ControlTypeFieldValue> values) {
    Map<String, Object> map = new LinkedHashMap<>();
    if (values == null) {
      return map;
    }
    for (ControlTypeFieldValue value : values) {
      ControlTypeField field = value.getControlTypeField();
      if (field == null || field.getCode() == null) {
        continue;
      }
      map.put(field.getCode(), getScriptValue(value, field));
    }
    return map;
  }

  protected Object getScriptValue(ControlTypeFieldValue value, ControlTypeField field) {
    switch (field.getTypeSelect()) {
      case ControlTypeFieldRepository.TYPE_DECIMAL:
        return value.getDecimalValue();
      case ControlTypeFieldRepository.TYPE_BOOLEAN:
        return Boolean.TRUE.equals(value.getBooleanValue());
      case ControlTypeFieldRepository.TYPE_SELECTION:
        return value.getSelectionValue();
      default:
        return value.getTextValue();
    }
  }

  @Override
  public boolean isEmpty(ControlTypeFieldValue value) {
    ControlTypeField field = value.getControlTypeField();
    if (field == null) {
      return true;
    }
    switch (field.getTypeSelect()) {
      case ControlTypeFieldRepository.TYPE_DECIMAL:
        return value.getDecimalValue() == null;
      case ControlTypeFieldRepository.TYPE_BOOLEAN:
        return value.getBooleanValue() == null;
      case ControlTypeFieldRepository.TYPE_SELECTION:
        return value.getSelectionValue() == null;
      default:
        return value.getTextValue() == null || value.getTextValue().isEmpty();
    }
  }

  @Override
  public List<String> getMissingRequiredFieldNames(
      ControlType controlType,
      Collection<ControlTypeFieldValue> planValues,
      Collection<ControlTypeFieldValue> entryValues) {

    List<String> missingFieldNames = new ArrayList<>();
    collectMissingRequiredFieldNames(
        toFields(getPlanFieldLines(controlType)), planValues, missingFieldNames);
    collectMissingRequiredFieldNames(
        toFields(getEntryFieldLines(controlType)), entryValues, missingFieldNames);
    return missingFieldNames;
  }

  protected List<ControlTypeField> toFields(List<ControlTypeFieldLine> lines) {
    return lines.stream()
        .map(ControlTypeFieldLine::getControlTypeField)
        .collect(Collectors.toList());
  }

  protected void collectMissingRequiredFieldNames(
      List<ControlTypeField> fields,
      Collection<ControlTypeFieldValue> values,
      List<String> missingFieldNames) {

    Map<ControlTypeField, ControlTypeFieldValue> valueMap = indexByField(values);

    fields.stream()
        .filter(field -> Boolean.TRUE.equals(field.getIsRequired()))
        .filter(field -> Optional.ofNullable(valueMap.get(field)).map(this::isEmpty).orElse(true))
        .map(ControlTypeField::getName)
        .forEach(missingFieldNames::add);
  }

  protected Map<ControlTypeField, ControlTypeFieldValue> indexByField(
      Collection<ControlTypeFieldValue> values) {

    Map<ControlTypeField, ControlTypeFieldValue> valueMap = new HashMap<>();
    if (values != null) {
      values.stream()
          .filter(value -> value.getControlTypeField() != null)
          .forEach(value -> valueMap.putIfAbsent(value.getControlTypeField(), value));
    }
    return valueMap;
  }

  @Override
  public void checkRequiredValues(
      ControlType controlType,
      Collection<ControlTypeFieldValue> planValues,
      Collection<ControlTypeFieldValue> entryValues)
      throws AxelorException {

    List<String> missingFieldNames =
        getMissingRequiredFieldNames(controlType, planValues, entryValues);

    if (!missingFieldNames.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(QualityExceptionMessage.CONTROL_TYPE_FIELD_VALUE_REQUIRED),
          String.join(", ", missingFieldNames));
    }
  }
}
