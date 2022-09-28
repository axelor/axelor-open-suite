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

import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.exception.AxelorException;

public interface AnalyticJournalControlService {

  /**
   * This method checks if name of analyticJournal for a company is already in database.
   *
   * @param analyticJournal
   * @throws AxelorException
   */
  void controlDuplicateCode(AnalyticJournal analyticJournal) throws AxelorException;

  /**
   * This method checks if analyticJournal is already use in a {@link AnalyticMoveLine}
   *
   * @param analyticJournal
   */
  boolean isInAnalyticMoveLine(AnalyticJournal analyticJournal);

  void toggleStatusSelect(AnalyticJournal analyticJournal);
}
