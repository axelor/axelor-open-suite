package com.axelor.apps.base.service;

import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;

public class DMSServiceImpl implements DMSService {

  protected DMSFileRepository dmsFileRepository;
  protected MetaFiles metaFiles;

  @Inject
  public DMSServiceImpl(DMSFileRepository dmsFileRepository, MetaFiles metaFiles) {
    this.dmsFileRepository = dmsFileRepository;
    this.metaFiles = metaFiles;
  }

  @Override
  public Long setDmsFile(MetaFile metaFile, Long dmsId, Model entity) {
    if (metaFile == null) {
      DMSFile toDelete = dmsFileRepository.find(dmsId);
      if (toDelete != null) {
        metaFiles.delete(toDelete);
      }
      return null;
    } else {
      DMSFile dmsFile = metaFiles.attach(metaFile, metaFile.getFileName(), entity);
      return dmsFile.getId();
    }
  }

  @Override
  public String getInlineUrl(Long id) {
    if (id == null || id == 0) {
      return "";
    }

    DMSFile dmsFile = dmsFileRepository.find(id);
    return String.format("ws/dms/inline/%d", dmsFile.getId());
  }
}
