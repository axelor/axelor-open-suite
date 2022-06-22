package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.DataBackup;

public class DataBackupManagementRepository extends DataBackupRepository {

  @Override
  public DataBackup copy(DataBackup entity, boolean deep) {
    DataBackup copy = super.copy(entity, deep);

    copy.setStatusSelect(DATA_BACKUP_STATUS_DRAFT);
    copy.setBackupMetaFile(null);
    copy.setBackupDate(null);
    copy.setLogMetaFile(null);

    return copy;
  }
}
