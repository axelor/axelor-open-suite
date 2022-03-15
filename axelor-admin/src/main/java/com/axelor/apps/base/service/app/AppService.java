/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.app;

import com.axelor.apps.base.db.App;
import com.axelor.apps.base.exceptions.IExceptionMessages;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface AppService {

  public App importDataDemo(App app) throws AxelorException, IOException;

  public Model getApp(String type);

  public boolean isApp(String type);

  public App installApp(App app, String language) throws AxelorException, IOException;

  public App unInstallApp(App app) throws AxelorException;

  public void refreshApp() throws IOException, ClassNotFoundException;

  public void bulkInstall(Collection<App> apps, Boolean importDeomo, String language)
      throws AxelorException, IOException;

  public App importRoles(App app) throws AxelorException, IOException;

  public void importRoles() throws AxelorException, IOException;

  public String getDataExportDir() throws AxelorException;

  public static String getFileUploadDir() throws AxelorException {
    String appSettingsPath = MetaFiles.getPath("tmp").getParent().toString();
    if (appSettingsPath == null || appSettingsPath.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessages.FILE_UPLOAD_DIR_ERROR));
    }
    return !appSettingsPath.endsWith(File.separator)
        ? appSettingsPath + File.separator
        : appSettingsPath;
  }
}
