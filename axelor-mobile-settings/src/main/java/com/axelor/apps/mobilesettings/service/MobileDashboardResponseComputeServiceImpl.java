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
package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.apps.mobilesettings.db.MobileDashboardLine;
import com.axelor.apps.mobilesettings.rest.dto.MobileChartResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileDashboardLineResponse;
import com.axelor.apps.mobilesettings.rest.dto.MobileDashboardResponse;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Role;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MobileDashboardResponseComputeServiceImpl
    implements MobileDashboardResponseComputeService {
  protected MobileChartResponseComputeService mobileChartResponseComputeService;

  @Inject
  public MobileDashboardResponseComputeServiceImpl(
      MobileChartResponseComputeService mobileChartResponseComputeService) {
    this.mobileChartResponseComputeService = mobileChartResponseComputeService;
  }

  @Override
  public Optional<MobileDashboardResponse> computeMobileDashboardResponse(
      MobileDashboard mobileDashboard) throws AxelorException {
    if (!checkAuthorizationToDashboard(mobileDashboard.getAuthorizedRoleSet())) {
      return Optional.empty();
    }
    List<MobileDashboardLineResponse> mobileDashboardLineResponseList = new ArrayList<>();
    for (MobileDashboardLine mobileDashboardLine : mobileDashboard.getMobileDashboardLineList()) {
      List<MobileChartResponse> mobileChartResponseList = new ArrayList<>();

      addMobileChartResponses(mobileDashboardLine, mobileChartResponseList);

      mobileDashboardLineResponseList.add(
          new MobileDashboardLineResponse(mobileDashboardLine.getName(), mobileChartResponseList));
    }
    return Optional.of(
        new MobileDashboardResponse(mobileDashboard, mobileDashboardLineResponseList));
  }

  protected boolean checkAuthorizationToDashboard(Set<Role> authorizedRoleSet) {
    if (authorizedRoleSet == null || authorizedRoleSet.isEmpty()) {
      return true;
    }
    return authorizedRoleSet.stream().anyMatch(AuthUtils.getUser().getRoles()::contains);
  }

  protected void addMobileChartResponses(
      MobileDashboardLine mobileDashboardLine, List<MobileChartResponse> mobileChartResponseList)
      throws AxelorException {
    addMobileChartResponse(mobileDashboardLine.getMobileChart1(), mobileChartResponseList);
    addMobileChartResponse(mobileDashboardLine.getMobileChart2(), mobileChartResponseList);
    addMobileChartResponse(mobileDashboardLine.getMobileChart3(), mobileChartResponseList);
    addMobileChartResponse(mobileDashboardLine.getMobileChart4(), mobileChartResponseList);
  }

  protected void addMobileChartResponse(
      MobileChart mobileChart, List<MobileChartResponse> mobileChartResponseList)
      throws AxelorException {
    if (mobileChart != null) {
      mobileChartResponseList.add(
          mobileChartResponseComputeService.computeMobileChartResponse(mobileChart));
    }
  }
}
