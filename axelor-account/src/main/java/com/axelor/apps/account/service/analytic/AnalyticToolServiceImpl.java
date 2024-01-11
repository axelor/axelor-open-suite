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
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.rpc.Context;
import com.axelor.utils.MapTools;
import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class AnalyticToolServiceImpl implements AnalyticToolService {

  protected AppAccountService appAccountService;
  protected AccountConfigService accountConfigService;

  @Inject
  public AnalyticToolServiceImpl(
      AppAccountService appAccountService, AccountConfigService accountConfigService) {
    this.appAccountService = appAccountService;
    this.accountConfigService = accountConfigService;
  }

  @Override
  public boolean isManageAnalytic(Company company) throws AxelorException {
    return appAccountService.getAppAccount().getManageAnalyticAccounting()
        && company != null
        && accountConfigService.getAccountConfig(company).getManageAnalyticAccounting();
  }

  @Override
  public boolean isPositionUnderAnalyticAxisSelect(Company company, int position)
      throws AxelorException {
    return company != null
        && position <= accountConfigService.getAccountConfig(company).getNbrOfAnalyticAxisSelect();
  }

  @Override
  public boolean isAxisAccountSumValidated(
      List<AnalyticMoveLine> analyticMoveLineList, AnalyticAxis analyticAxis) {
    if (!CollectionUtils.isEmpty(analyticMoveLineList)) {
      BigDecimal sum = BigDecimal.ZERO;
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        if (analyticMoveLine.getAnalyticAxis().equals(analyticAxis)) {
          sum = sum.add(analyticMoveLine.getPercentage());
        }
      }
      return sum.compareTo(new BigDecimal(100)) == 0;
    }
    return true;
  }

  @Override
  public boolean isAnalyticAxisFilled(
      AnalyticAccount analyticAccount, List<AnalyticMoveLine> analyticMoveLineList) {
    return analyticAccount != null
        && !isAxisAccountSumValidated(analyticMoveLineList, analyticAccount.getAnalyticAxis());
  }

  @Override
  public <T> T getFieldFromContextParent(Context context, String fieldName, Class<T> klass) {
    while (context != null) {
      T object = MapTools.get(context, klass, fieldName);
      if (object != null) {
        return object;
      }
      context = context.getParent();
    }
    return null;
  }
}
