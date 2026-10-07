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
package com.axelor.apps.base.service.app;

import com.axelor.app.AppSettings;
import com.axelor.app.AvailableAppSettings;
import com.axelor.apps.app.db.App;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Lifecycle of the apps declared by the modules in their {@code apps/*.yml} files: creation at
 * startup, installation with their dependencies, uninstallation and data imports.
 */
public interface AppService {

  App importDataDemo(App app) throws IOException;

  Model getApp(String code);

  boolean isApp(String code);

  App installApp(App app, String language) throws IOException;

  App unInstallApp(App app);

  /** Creates or updates the apps from the {@code apps/*.yml} files of every module. */
  void initApps() throws IOException;

  void bulkInstall(Collection<App> apps, boolean importDemo, String language) throws IOException;

  App importRoles(App app) throws IOException;

  void importRoles() throws IOException;

  /** Language used to import the app data when the app has none: the application locale. */
  String getApplicationLocale();

  static String getFileUploadDir() {
    String dataUploadDirPath = AppSettings.get().get(AvailableAppSettings.DATA_UPLOAD_DIR);
    if (dataUploadDirPath.isEmpty()) {
      throw new IllegalStateException(I18n.get(BaseExceptionMessage.FILE_UPLOAD_DIR_ERROR));
    }
    return !dataUploadDirPath.endsWith(File.separator)
        ? dataUploadDirPath + File.separator
        : dataUploadDirPath;
  }
}
