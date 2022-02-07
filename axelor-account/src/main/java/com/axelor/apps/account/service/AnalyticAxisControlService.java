/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.exception.AxelorException;

public interface AnalyticAxisControlService {

  /**
   * Method that checks unicity by code and company of analyticAxis.
   *
   * @param analyticAxis
   * @throws AxelorException
   */
  void controlUnicity(AnalyticAxis analyticAxis) throws AxelorException;

  /**
   * Method that checks if analyticAxis is in a {@link AnalyticMoveLine}
   *
   * @param analyticAxis
   * @return true if analyticAxis is used in a analyticMoveLine, false else.
   */
  boolean isInAnalyticMoveLine(AnalyticAxis analyticAxis);
}
