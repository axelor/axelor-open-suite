package com.axelor.apps.base.service;

import com.axelor.db.Model;
import com.axelor.meta.db.MetaFile;

public interface DMSService {
  Long setDmsFile(MetaFile metaFile, Long dmsId, Model entity);

  String getInlineUrl(Long id);
}
