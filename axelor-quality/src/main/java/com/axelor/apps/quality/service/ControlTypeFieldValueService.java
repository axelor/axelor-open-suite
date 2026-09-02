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
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.db.ControlType;
import com.axelor.apps.quality.db.ControlTypeField;
import com.axelor.apps.quality.db.ControlTypeFieldValue;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ControlTypeFieldValueService {

  /**
   * Fields of the control type having the given usage, ordered by sequence then name.
   *
   * @param controlType the control type, may be null
   * @param usageSelect {@link com.axelor.apps.quality.db.repo.ControlTypeFieldRepository}
   *     USAGE_PLAN or USAGE_ENTRY
   */
  List<ControlTypeField> getFields(ControlType controlType, int usageSelect);

  /**
   * Rebuilds the reference values of a control plan line from its control type: values of fields
   * still present in the type are kept, values of fields no longer in it are dropped, missing ones
   * are created empty.
   */
  List<ControlTypeFieldValue> syncPlanValues(
      ControlType controlType, List<ControlTypeFieldValue> currentValues);

  /** Creates the empty measured values of an entry sample line from its control type. */
  void createEntryValues(ControlEntryPlanLine entryLine, ControlType controlType);

  ControlTypeFieldValue createValue(ControlTypeField controlTypeField);

  ControlTypeFieldValue copyValue(ControlTypeFieldValue controlTypeFieldValue);

  /** Values indexed by field code, typed according to the field type, to feed a formula. */
  Map<String, Object> toScriptMap(Collection<ControlTypeFieldValue> values);

  boolean isEmpty(ControlTypeFieldValue controlTypeFieldValue);

  /**
   * Names of the required fields of the control type whose value is empty or missing. A field added
   * to the control type after the lines were created has no value at all: it is reported here too,
   * otherwise the formula would silently read a null value.
   */
  List<String> getMissingRequiredFieldNames(
      ControlType controlType,
      Collection<ControlTypeFieldValue> planValues,
      Collection<ControlTypeFieldValue> entryValues);

  /**
   * Checks that every required field of the control type has a filled value.
   *
   * @throws AxelorException if at least one required value is empty or missing
   */
  void checkRequiredValues(
      ControlType controlType,
      Collection<ControlTypeFieldValue> planValues,
      Collection<ControlTypeFieldValue> entryValues)
      throws AxelorException;
}
