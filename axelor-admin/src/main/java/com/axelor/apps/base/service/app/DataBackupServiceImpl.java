/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.DataBackup;
import com.axelor.apps.base.db.repo.DataBackupRepository;
import com.axelor.auth.AuditableRunner;
import com.axelor.db.JPA;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBackupServiceImpl implements DataBackupService {

  @Inject DataBackupCreateService createService;

  @Inject DataBackupRestoreService restoreService;

  @Inject private MetaFiles metaFiles;

  @Inject DataBackupRepository dataBackupRepository;

  private ExecutorService executor = Executors.newCachedThreadPool();

  static String configFileName = "config.xml";

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void createBackUp(DataBackup dataBackup) {
    DataBackup obj = dataBackupRepository.find(dataBackup.getId());
    obj.setStatusSelect(DataBackupRepository.DATA_BACKUP_STATUS_IN_PROGRESS);
    dataBackupRepository.save(obj);

    try {
      executor.submit(
          new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
              startBackup(dataBackup);
              return true;
            }
          });
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void startBackup(DataBackup dataBackup) throws Exception {
    final AuditableRunner runner = Beans.get(AuditableRunner.class);
    final Callable<Boolean> job =
        new Callable<Boolean>() {
          @Override
          public Boolean call() throws Exception {
            Logger LOG = LoggerFactory.getLogger(getClass());
            DataBackup obj = Beans.get(DataBackupRepository.class).find(dataBackup.getId());
            File backupFile = createService.create(obj.getFetchLimit());
            obj.setBackupMetaFile(metaFiles.upload(backupFile));
            obj.setStatusSelect(DataBackupRepository.DATA_BACKUP_STATUS_CREATED);
            Beans.get(DataBackupRepository.class).save(obj);
            LOG.info("Data BackUp Saved");
            return true;
          }
        };
    runner.run(job);
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void restoreBackUp(DataBackup dataBackup) {
    DataBackup obj = dataBackupRepository.find(dataBackup.getId());
    obj.setStatusSelect(DataBackupRepository.DATA_BACKUP_STATUS_IN_PROGRESS);
    dataBackupRepository.save(obj);

    try {
      executor.submit(
          new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
              startRestore(dataBackup);
              return true;
            }
          });
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  protected void startRestore(DataBackup dataBackup) throws Exception {
    final AuditableRunner runner = Beans.get(AuditableRunner.class);
    final Callable<Boolean> job =
        new Callable<Boolean>() {
          @Override
          public Boolean call() throws Exception {
            Logger LOG = LoggerFactory.getLogger(getClass());
            DataBackup obj = Beans.get(DataBackupRepository.class).find(dataBackup.getId());
            File logFile = restoreService.restore(obj.getBackupMetaFile());
            save(logFile, obj);
            LOG.info("Data Restore Saved");
            return true;
          }

          public void save(File logFile, DataBackup obj) {
            try {
              JPA.em().getTransaction().begin();
              obj = Beans.get(DataBackupRepository.class).find(dataBackup.getId());
              if (logFile != null) {
                obj.setStatusSelect(DataBackupRepository.DATA_BACKUP_STATUS_RESTORE);
                obj.setLogMetaFile(metaFiles.upload(logFile));
              } else {
                obj.setStatusSelect(DataBackupRepository.DATA_BACKUP_STATUS_RESTORE_ERROR);
              }
              Beans.get(DataBackupRepository.class).save(obj);
              JPA.em().getTransaction().commit();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        };
    runner.run(job);
  }

  public boolean SeuencesExist() {
    return restoreService.SeuencesExist();
  }
}
