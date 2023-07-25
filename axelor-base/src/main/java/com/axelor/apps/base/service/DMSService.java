package com.axelor.apps.base.service;

import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import com.axelor.meta.db.MetaFile;

public interface DMSService {
  DMSFile setDmsFile(MetaFile metaFile, DMSFile dmsFile, Model entity);

  String getInlineUrl(DMSFile dmsFile);
}
