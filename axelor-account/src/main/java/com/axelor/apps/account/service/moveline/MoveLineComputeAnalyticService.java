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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface MoveLineComputeAnalyticService {

  MoveLine computeAnalyticDistribution(MoveLine moveLine);

  MoveLine createAnalyticDistributionWithTemplate(MoveLine moveLine);

  void updateAccountTypeOnAnalytic(MoveLine moveLine, List<AnalyticMoveLine> analyticMoveLineList);

  void generateAnalyticMoveLines(MoveLine moveLine);

  MoveLine selectDefaultDistributionTemplate(MoveLine moveLine) throws AxelorException;

  List<Long> setAxisDomains(MoveLine moveline, int position) throws AxelorException;

  boolean compareNbrOfAnalyticAxisSelect(int position, MoveLine moveLine) throws AxelorException;

  public MoveLine analyzeMoveLine(MoveLine moveLine) throws AxelorException;
}
