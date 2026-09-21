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
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.rest.dto.ControlEntrySampleLinePutRequest;

public interface ControlEntrySampleLineUpdateAPIService {

  /**
   * Saves the measured values sent in the request on the control entry sample line, evaluates the
   * conformity of the line and propagates the result to its control entry sample, as the check
   * conformity button does. Everything is done in one transaction: nothing is saved if a value is
   * rejected or if the conformity can not be evaluated.
   *
   * @return the updated line, with its result and the result of its sample
   */
  ControlEntryPlanLine updateValuesAndCheckConformity(
      ControlEntryPlanLine controlEntrySampleLine, ControlEntrySampleLinePutRequest request)
      throws AxelorException;
}
