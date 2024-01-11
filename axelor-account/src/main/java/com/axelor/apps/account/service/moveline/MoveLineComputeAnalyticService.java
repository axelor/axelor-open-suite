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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import java.math.BigDecimal;
import java.util.List;

public interface MoveLineComputeAnalyticService {

  MoveLine computeAnalyticDistribution(MoveLine moveLine);

  void computeAnalyticDistribution(MoveLine moveLine, Move move) throws AxelorException;

  MoveLine createAnalyticDistributionWithTemplate(MoveLine moveLine);

  MoveLine createAnalyticDistributionWithTemplate(MoveLine moveLine, Move move)
      throws AxelorException;

  void updateAccountTypeOnAnalytic(MoveLine moveLine, List<AnalyticMoveLine> analyticMoveLineList);

  void generateAnalyticMoveLines(MoveLine moveLine);

  MoveLine selectDefaultDistributionTemplate(MoveLine moveLine) throws AxelorException;

  MoveLine analyzeMoveLine(MoveLine moveLine, Company company) throws AxelorException;

  public BigDecimal getAnalyticAmount(MoveLine moveLine, AnalyticMoveLine analyticMoveLine);

  MoveLine clearAnalyticAccounting(MoveLine moveLine);

  MoveLine clearAnalyticAccountingIfEmpty(MoveLine moveLine);

  boolean checkManageAnalytic(Company company) throws AxelorException;
}
