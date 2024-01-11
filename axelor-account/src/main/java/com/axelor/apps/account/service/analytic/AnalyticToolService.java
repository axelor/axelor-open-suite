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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.rpc.Context;
import java.util.List;

public interface AnalyticToolService {

  boolean isManageAnalytic(Company company) throws AxelorException;

  boolean isPositionUnderAnalyticAxisSelect(Company company, int position) throws AxelorException;

  boolean isAxisAccountSumValidated(
      List<AnalyticMoveLine> analyticMoveLineList, AnalyticAxis analyticAxis);

  boolean isAnalyticAxisFilled(
      AnalyticAccount analyticAccount, List<AnalyticMoveLine> analyticMoveLineList);

  <T> T getFieldFromContextParent(Context context, String fieldName, Class<T> klass);
}
