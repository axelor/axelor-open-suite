/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticJournal;
import java.math.BigDecimal;

public class AnalyticDistributionLineServiceImpl implements AnalyticDistributionLineService {

  @Override
  public AnalyticDistributionLine createAnalyticDistributionLine(
      AnalyticAxis analyticAxis,
      AnalyticAccount analyticAccount,
      AnalyticJournal analyticJournal,
      BigDecimal percentage) {
    AnalyticDistributionLine analyticDistributionLine = new AnalyticDistributionLine();
    analyticDistributionLine.setAnalyticAxis(analyticAxis);
    analyticDistributionLine.setAnalyticAccount(analyticAccount);
    analyticDistributionLine.setAnalyticJournal(analyticJournal);
    analyticDistributionLine.setPercentage(percentage);
    return analyticDistributionLine;
  }
}
