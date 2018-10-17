/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.File;

public class DataBackupServiceImpl implements DataBackupService {

  @Inject DataBackupCreateService createService;

  @Inject DataBackupRestoreService restoreService;

  static final String CONFIG_FILE_NAME = "config.xml";

  @Override
  public File createBackUp(Integer fetchLimit) {
    return createService.create(fetchLimit);
  }

  @Override
  public File restoreBackUp(MetaFile zipedBackupFile) {
    return restoreService.restore(zipedBackupFile);
  }

  public boolean SeuencesExist() {
    return restoreService.SeuencesExist();
  }
}
