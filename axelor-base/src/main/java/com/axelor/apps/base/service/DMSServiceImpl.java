package com.axelor.apps.base.service;

import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;

public class DMSServiceImpl implements DMSService {

  protected MetaFiles metaFiles;

  @Inject
  public DMSServiceImpl(MetaFiles metaFiles) {
    this.metaFiles = metaFiles;
  }

  @Override
  public DMSFile setDmsFile(MetaFile metaFile, DMSFile dmsFile, Model entity) {
    if (metaFile == null) {
      if (dmsFile != null) {
        metaFiles.delete(dmsFile);
      }
      return null;
    } else {
      return metaFiles.attach(metaFile, metaFile.getFileName(), entity);
    }
  }

  @Override
  public String getInlineUrl(DMSFile dmsFile) {
    if (dmsFile == null || dmsFile.getId() == null) {
      return "";
    }
    return String.format("ws/dms/inline/%d", dmsFile.getId());
  }
}
