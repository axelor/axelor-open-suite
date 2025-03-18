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
package com.axelor.apps.mobilesettings.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.mobilesettings.service.AppMobileSettingsService;
import com.axelor.apps.mobilesettings.service.AppMobileSettingsServiceImpl;
import com.axelor.apps.mobilesettings.service.MobileChartResponseComputeService;
import com.axelor.apps.mobilesettings.service.MobileChartResponseComputeServiceImpl;
import com.axelor.apps.mobilesettings.service.MobileChartService;
import com.axelor.apps.mobilesettings.service.MobileChartServiceImpl;
import com.axelor.apps.mobilesettings.service.MobileDashboardResponseComputeService;
import com.axelor.apps.mobilesettings.service.MobileDashboardResponseComputeServiceImpl;
import com.axelor.apps.mobilesettings.service.MobileMenuCreateService;
import com.axelor.apps.mobilesettings.service.MobileMenuCreateServiceImpl;
import com.axelor.apps.mobilesettings.service.MobileScreenCreateService;
import com.axelor.apps.mobilesettings.service.MobileScreenCreateServiceImpl;
import com.axelor.apps.mobilesettings.service.MobileSettingsResponseComputeService;
import com.axelor.apps.mobilesettings.service.MobileSettingsResponseComputeServiceImpl;
import com.axelor.apps.mobilesettings.service.UserDMSFileService;
import com.axelor.apps.mobilesettings.service.UserDMSFileServiceImpl;

public class MobileSettingsModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(AppMobileSettingsService.class).to(AppMobileSettingsServiceImpl.class);
    bind(MobileChartService.class).to(MobileChartServiceImpl.class);
    bind(MobileChartResponseComputeService.class).to(MobileChartResponseComputeServiceImpl.class);
    bind(MobileDashboardResponseComputeService.class)
        .to(MobileDashboardResponseComputeServiceImpl.class);
    bind(MobileSettingsResponseComputeService.class)
        .to(MobileSettingsResponseComputeServiceImpl.class);
    bind(MobileMenuCreateService.class).to(MobileMenuCreateServiceImpl.class);
    bind(MobileScreenCreateService.class).to(MobileScreenCreateServiceImpl.class);
    bind(UserDMSFileService.class).to(UserDMSFileServiceImpl.class);
  }
}
