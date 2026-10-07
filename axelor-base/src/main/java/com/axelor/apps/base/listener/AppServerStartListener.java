/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.listener;

import com.axelor.app.AppSettings;
import com.axelor.apps.app.db.App;
import com.axelor.apps.app.db.repo.AppRepository;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.common.StringUtils;
import com.axelor.event.Observes;
import com.axelor.events.StartupEvent;
import com.axelor.utils.helpers.ExceptionHelper;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Creates the apps declared in the {@code apps/*.yml} files of the modules, then installs the apps
 * listed in the {@code studio.apps.install} property ({@code all} for every app).
 */
public class AppServerStartListener {

  protected static final String APPS_INSTALL = "studio.apps.install";

  /** Former name of {@link #APPS_INSTALL}, still read when the latter is not set. */
  protected static final String APPS_INSTALL_LEGACY = "aos.apps.install-apps";

  protected static final String IMPORT_DEMO_DATA = "data.import.demo-data";

  protected final AppService appService;
  protected final AppRepository appRepository;

  @Inject
  public AppServerStartListener(AppService appService, AppRepository appRepository) {
    this.appService = appService;
    this.appRepository = appRepository;
  }

  public void onStartUp(@Observes @Priority(value = -1) StartupEvent event) {
    try {
      appService.initApps();
    } catch (Exception e) {
      ExceptionHelper.error(e);
    }
  }

  public void installAppsOnStartup(@Observes StartupEvent event) {

    final RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());

    try (RequestScoper.CloseableScope ignored = scope.open()) {

      String apps = getAppsToInstall();
      if (StringUtils.isBlank(apps)) {
        return;
      }

      List<App> appList;
      if (apps.equalsIgnoreCase("all")) {
        appList = appRepository.all().filter("self.active IS NULL OR self.active = false").fetch();
      } else {
        appList =
            Arrays.stream(apps.split(","))
                .map(code -> appRepository.findByCode(code.trim()))
                .filter(Objects::nonNull)
                .toList();
      }

      if (appList.isEmpty()) {
        return;
      }

      appService.bulkInstall(
          appList,
          AppSettings.get().getBoolean(IMPORT_DEMO_DATA, false),
          appService.getApplicationLocale());

    } catch (Exception e) {
      ExceptionHelper.error(e);
    }
  }

  protected String getAppsToInstall() {
    String appsToInstall = AppSettings.get().get(APPS_INSTALL);
    if (StringUtils.isBlank(appsToInstall)) {
      appsToInstall = AppSettings.get().get(APPS_INSTALL_LEGACY);
    }
    return appsToInstall;
  }
}
