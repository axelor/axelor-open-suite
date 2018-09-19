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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.DataBackup;
import com.axelor.apps.base.db.repo.DataBackupRepository;
import com.axelor.apps.base.service.app.DataBackupService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;

public class DataBackupController {

  @Inject DataBackupService dataBackupService;

  @Inject private MetaFiles metaFiles;

  public void CreateBackUp(ActionRequest req, ActionResponse res) throws IOException {
    DataBackup dataBackup = req.getContext().asType(DataBackup.class);
    File zipFile = dataBackupService.createBackUp(dataBackup.getFetchLimit());
    MetaFile backupFile = metaFiles.upload(zipFile);
    res.setValue("backupMetaFile", backupFile);
    res.setValue("statusSelect", DataBackupRepository.DATA_BACKUP_STATUS_CREATED);
  }

  public void RestoreBackUp(ActionRequest req, ActionResponse res) throws IOException {
    if (dataBackupService.SeuencesExist()) {
      res.setError("Remove all Sequences And MrpLineTypes to restore backup");
    } else {
      DataBackup obj = req.getContext().asType(DataBackup.class);
      File logFile = dataBackupService.restoreBackUp(obj.getBackupMetaFile());
      if (logFile != null) {
        res.setValue("statusSelect", DataBackupRepository.DATA_BACKUP_STATUS_RESTORE);
        MetaFile logMetaFile = metaFiles.upload(logFile);
        res.setValue("logMetaFile", logMetaFile);
      } else {
        res.setValue("statusSelect", DataBackupRepository.DATA_BACKUP_STATUS_RESTORE_ERROR);
      }
    }
  }
}
