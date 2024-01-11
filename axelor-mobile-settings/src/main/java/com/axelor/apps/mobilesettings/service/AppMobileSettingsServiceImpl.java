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

import com.axelor.apps.mobilesettings.db.MobileConfig;
import com.axelor.apps.mobilesettings.db.repo.MobileConfigRepository;
import com.axelor.studio.db.AppMobileSettings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class AppMobileSettingsServiceImpl implements AppMobileSettingsService {

  private final MobileConfigRepository mobileConfigRepository;

  @Inject
  public AppMobileSettingsServiceImpl(MobileConfigRepository mobileConfigRepository) {
    this.mobileConfigRepository = mobileConfigRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateMobileConfig(Boolean isAppActivated, String sequence) {
    MobileConfig mobileConfig =
        mobileConfigRepository.all().filter("self.sequence = ?", sequence).fetchOne();
    if (!isAppActivated.equals(mobileConfig.getIsAppEnabled())) {
      mobileConfig.setIsAppEnabled(isAppActivated);
      mobileConfigRepository.save(mobileConfig);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateAllMobileConfig(AppMobileSettings appMobileSettings) {
    updateMobileConfig(
        appMobileSettings.getIsStockAppEnabled(), MobileConfigRepository.APP_SEQUENCE_STOCK);
    updateMobileConfig(
        appMobileSettings.getIsProductionAppEnabled(),
        MobileConfigRepository.APP_SEQUENCE_MANUFACTURING);
    updateMobileConfig(
        appMobileSettings.getIsCrmAppEnabled(), MobileConfigRepository.APP_SEQUENCE_CRM);
  }
}
