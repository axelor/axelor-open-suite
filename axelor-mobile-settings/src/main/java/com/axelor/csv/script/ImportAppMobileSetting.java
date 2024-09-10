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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.studio.db.AppMobileSettings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Map;

public class ImportAppMobileSetting {

  @Inject protected MetaModuleRepository metaModuleRepository;

  @Transactional(rollbackOn = {Exception.class})
  public Object getMinimalVersion(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof AppMobileSettings;
    AppMobileSettings appMobileSettings = (AppMobileSettings) bean;

    String mobileSettingModule = "axelor-mobile-settings";
    int beginIndex = 0;
    int endIndex = 4;
    String minimalVersionEnding = "0";

    MetaModule metaModule =
        metaModuleRepository.all().filter("self.name=?", mobileSettingModule).fetchOne();
    String moduleVersion =
        metaModule.getModuleVersion().substring(beginIndex, endIndex) + minimalVersionEnding;
    appMobileSettings.setMinimalRequiredMobileAppVersion(moduleVersion);
    return appMobileSettings;
  }
}
