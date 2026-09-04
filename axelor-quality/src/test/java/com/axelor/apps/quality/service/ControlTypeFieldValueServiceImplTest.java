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

import com.axelor.apps.quality.db.CharacteristicProperty;
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.db.ControlType;
import com.axelor.apps.quality.db.ControlTypeField;
import com.axelor.apps.quality.db.ControlTypeFieldValue;
import com.axelor.apps.quality.db.repo.ControlTypeFieldRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ControlTypeFieldValueServiceImplTest {

  private static final int USAGE_PLAN = 1;
  private static final int USAGE_ENTRY = 2;
  private static final Map<ControlTypeField, Integer> USAGES = new HashMap<>();

  private static ControlTypeFieldValueService controlTypeFieldValueService;

  private static ControlTypeField nominalDimension;
  private static ControlTypeField minTolerance;
  private static ControlTypeField measuredDimension;
  private static ControlTypeField acceptedColor;
  private static ControlTypeField labelRequired;
  private static ControlTypeField externalCondition;

  @BeforeAll
  static void prepare() {
    controlTypeFieldValueService = new ControlTypeFieldValueServiceImpl();

    nominalDimension =
        createField(
            "Nominal dimension",
            "acceptedDimension",
            ControlTypeFieldRepository.TYPE_DECIMAL,
            USAGE_PLAN,
            2);
    minTolerance =
        createField(
            "Min tolerance",
            "minTolerance",
            ControlTypeFieldRepository.TYPE_DECIMAL,
            USAGE_PLAN,
            1);
    measuredDimension =
        createField(
            "Measured dimension",
            "mesuredDim",
            ControlTypeFieldRepository.TYPE_DECIMAL,
            USAGE_ENTRY,
            1);
    acceptedColor =
        createField(
            "Accepted color", "acceptedColor", ControlTypeFieldRepository.TYPE_TEXT, USAGE_PLAN, 3);
    labelRequired =
        createField(
            "Label required",
            "isLabelRequired",
            ControlTypeFieldRepository.TYPE_BOOLEAN,
            USAGE_PLAN,
            4);
    externalCondition =
        createField(
            "Accepted external condition",
            "acceptedExternalCondition",
            ControlTypeFieldRepository.TYPE_SELECTION,
            USAGE_PLAN,
            5);
  }

  protected static ControlTypeField createField(
      String name, String code, int typeSelect, int usageSelect, int sequence) {
    ControlTypeField field = new ControlTypeField();
    field.setName(name);
    field.setCode(code);
    field.setTypeSelect(typeSelect);
    field.setSequence(sequence);
    USAGES.put(field, usageSelect);
    return field;
  }

  /** The usage is carried by the collection holding the field, not by the field itself. */
  protected static ControlType createControlType(ControlTypeField... fields) {
    ControlType controlType = new ControlType();
    controlType.setPlanFieldSet(
        Arrays.stream(fields)
            .filter(field -> USAGES.get(field) == USAGE_PLAN)
            .collect(Collectors.toCollection(LinkedHashSet::new)));
    controlType.setEntryFieldSet(
        Arrays.stream(fields)
            .filter(field -> USAGES.get(field) == USAGE_ENTRY)
            .collect(Collectors.toCollection(LinkedHashSet::new)));
    return controlType;
  }

  @Test
  void testGetFieldsSplitsOnTheCollectionAndOrdersOnSequence() {
    ControlType controlType =
        createControlType(nominalDimension, minTolerance, measuredDimension, acceptedColor);

    List<ControlTypeField> planFields = controlTypeFieldValueService.getPlanFields(controlType);

    Assertions.assertEquals(
        Arrays.asList("minTolerance", "acceptedDimension", "acceptedColor"),
        planFields.stream().map(ControlTypeField::getCode).collect(Collectors.toList()));

    List<ControlTypeField> entryFields = controlTypeFieldValueService.getEntryFields(controlType);

    Assertions.assertEquals(1, entryFields.size());
    Assertions.assertEquals("mesuredDim", entryFields.get(0).getCode());
  }

  @Test
  void testGetFieldsWithoutControlType() {
    Assertions.assertTrue(controlTypeFieldValueService.getPlanFields(null).isEmpty());
  }

  @Test
  void testSyncPlanValuesKeepsFilledValuesAndDropsObsoleteOnes() {
    ControlTypeFieldValue filledValue = controlTypeFieldValueService.createValue(minTolerance);
    filledValue.setDecimalValue(BigDecimal.ONE);
    ControlTypeFieldValue obsoleteValue = controlTypeFieldValueService.createValue(acceptedColor);
    obsoleteValue.setTextValue("Blue");

    List<ControlTypeFieldValue> values =
        controlTypeFieldValueService.syncPlanValues(
            createControlType(nominalDimension, minTolerance, measuredDimension),
            Arrays.asList(filledValue, obsoleteValue));

    Assertions.assertEquals(
        Arrays.asList("minTolerance", "acceptedDimension"),
        values.stream()
            .map(value -> value.getControlTypeField().getCode())
            .collect(Collectors.toList()));
    Assertions.assertSame(filledValue, values.get(0));
    Assertions.assertNull(values.get(1).getDecimalValue());
  }

  @Test
  void testSyncPlanValuesWithoutCurrentValues() {
    List<ControlTypeFieldValue> values =
        controlTypeFieldValueService.syncPlanValues(createControlType(minTolerance), null);

    Assertions.assertEquals(1, values.size());
    Assertions.assertEquals(minTolerance, values.get(0).getControlTypeField());
  }

  @Test
  void testCreateEntryValuesOnlyCreatesMeasuredValues() {
    ControlEntryPlanLine entryLine = new ControlEntryPlanLine();

    controlTypeFieldValueService.createEntryValues(
        entryLine, createControlType(nominalDimension, minTolerance, measuredDimension));

    Assertions.assertEquals(1, entryLine.getEntryValueList().size());
    Assertions.assertEquals(
        measuredDimension, entryLine.getEntryValueList().get(0).getControlTypeField());
    Assertions.assertNull(entryLine.getPlanValueList());
  }

  @Test
  void testToScriptMapTypesTheValuesOnTheFieldCode() {
    CharacteristicProperty scratched = new CharacteristicProperty();
    scratched.setName("Scratched");

    ControlTypeFieldValue decimalValue = controlTypeFieldValueService.createValue(minTolerance);
    decimalValue.setDecimalValue(new BigDecimal("0.500"));
    ControlTypeFieldValue textValue = controlTypeFieldValueService.createValue(acceptedColor);
    textValue.setTextValue("Blue");
    ControlTypeFieldValue booleanValue = controlTypeFieldValueService.createValue(labelRequired);
    ControlTypeFieldValue selectionValue =
        controlTypeFieldValueService.createValue(externalCondition);
    selectionValue.setSelectionValue(scratched);

    Map<String, Object> map =
        controlTypeFieldValueService.toScriptMap(
            Arrays.asList(decimalValue, textValue, booleanValue, selectionValue));

    Assertions.assertEquals(new BigDecimal("0.500"), map.get("minTolerance"));
    Assertions.assertEquals("Blue", map.get("acceptedColor"));
    Assertions.assertEquals(Boolean.FALSE, map.get("isLabelRequired"));
    Assertions.assertEquals(scratched, map.get("acceptedExternalCondition"));
  }

  @Test
  void testToScriptMapWithoutValues() {
    Assertions.assertTrue(controlTypeFieldValueService.toScriptMap(null).isEmpty());
    Assertions.assertTrue(
        controlTypeFieldValueService.toScriptMap(Collections.emptyList()).isEmpty());
  }

  @Test
  void testIsEmptyDistinguishesEmptyValuesFromZeroAndFalse() {
    ControlTypeFieldValue decimalValue = controlTypeFieldValueService.createValue(minTolerance);
    Assertions.assertTrue(controlTypeFieldValueService.isEmpty(decimalValue));
    decimalValue.setDecimalValue(BigDecimal.ZERO);
    Assertions.assertFalse(controlTypeFieldValueService.isEmpty(decimalValue));

    ControlTypeFieldValue booleanValue = controlTypeFieldValueService.createValue(labelRequired);
    Assertions.assertTrue(controlTypeFieldValueService.isEmpty(booleanValue));
    booleanValue.setBooleanValue(Boolean.FALSE);
    Assertions.assertFalse(controlTypeFieldValueService.isEmpty(booleanValue));

    ControlTypeFieldValue textValue = controlTypeFieldValueService.createValue(acceptedColor);
    Assertions.assertTrue(controlTypeFieldValueService.isEmpty(textValue));
    textValue.setTextValue("");
    Assertions.assertTrue(controlTypeFieldValueService.isEmpty(textValue));
    textValue.setTextValue("Blue");
    Assertions.assertFalse(controlTypeFieldValueService.isEmpty(textValue));
  }

  @Test
  void testCopyValueKeepsFieldAndValue() {
    ControlTypeFieldValue value = controlTypeFieldValueService.createValue(minTolerance);
    value.setDecimalValue(new BigDecimal("1.250"));

    ControlTypeFieldValue copy = controlTypeFieldValueService.copyValue(value);

    Assertions.assertNotSame(value, copy);
    Assertions.assertEquals(minTolerance, copy.getControlTypeField());
    Assertions.assertEquals(new BigDecimal("1.250"), copy.getDecimalValue());
    Assertions.assertNull(copy.getPlanLine());
    Assertions.assertNull(copy.getEntryLine());
  }

  @Test
  void testGetMissingRequiredFieldNamesIgnoresFilledAndOptionalFields() {
    ControlTypeField requiredReference = createRequiredField("requiredDimension");
    ControlTypeFieldValue filledValue = controlTypeFieldValueService.createValue(requiredReference);
    filledValue.setDecimalValue(BigDecimal.TEN);

    Assertions.assertTrue(
        controlTypeFieldValueService
            .getMissingRequiredFieldNames(
                createControlType(requiredReference, minTolerance),
                Arrays.asList(filledValue, controlTypeFieldValueService.createValue(minTolerance)),
                null)
            .isEmpty());
  }

  @Test
  void testGetMissingRequiredFieldNamesReportsEmptyValues() {
    ControlTypeField requiredReference = createRequiredField("requiredDimension");

    Assertions.assertEquals(
        Collections.singletonList("requiredDimension"),
        controlTypeFieldValueService.getMissingRequiredFieldNames(
            createControlType(requiredReference),
            Collections.singletonList(controlTypeFieldValueService.createValue(requiredReference)),
            null));
  }

  @Test
  void testGetMissingRequiredFieldNamesReportsFieldsWithoutAnyValue() {
    ControlTypeField requiredReference = createRequiredField("requiredDimension");
    ControlTypeField requiredMeasure = createRequiredField("requiredMeasure");
    USAGES.put(requiredMeasure, USAGE_ENTRY);

    // the fields were added to the control type after the lines were created: no value at all
    Assertions.assertEquals(
        Arrays.asList("requiredDimension", "requiredMeasure"),
        controlTypeFieldValueService.getMissingRequiredFieldNames(
            createControlType(requiredReference, requiredMeasure),
            Collections.emptyList(),
            Collections.emptyList()));
  }

  @Test
  void testCreateValueCopiesTheSequenceOfTheField() {
    Assertions.assertEquals(
        nominalDimension.getSequence(),
        controlTypeFieldValueService.createValue(nominalDimension).getSequence());
  }

  protected static ControlTypeField createRequiredField(String name) {
    ControlTypeField field =
        createField(name, name, ControlTypeFieldRepository.TYPE_DECIMAL, USAGE_PLAN, 1);
    field.setIsRequired(true);
    return field;
  }
}
