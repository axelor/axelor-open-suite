/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartResponse;
import com.axelor.meta.db.MetaAction;
import com.google.inject.Inject;

public class MobileChartResponseComputeServiceImpl implements MobileChartResponseComputeService {
  protected MobileChartService mobileChartService;

  @Inject
  public MobileChartResponseComputeServiceImpl(MobileChartService mobileChartService) {
    this.mobileChartService = mobileChartService;
  }

  @Override
  public MobileChartResponse computeMobileChartResponse(MobileChart mobileChart)
      throws AxelorException {
    if (mobileChart.getIsCustomChart()) {
      return new MobileChartResponse(
          mobileChart, mobileChart.getName(), mobileChartService.getValueList(mobileChart));
    } else {
      MetaAction metaAction = mobileChart.getChartMetaAction();
      String metaActionName = metaAction != null ? metaAction.getName() : null;
      return new MobileChartResponse(mobileChart, mobileChart.getName(), metaActionName);
    }
  }
}
