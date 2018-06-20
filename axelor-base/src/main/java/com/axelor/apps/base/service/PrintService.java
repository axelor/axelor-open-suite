package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Print;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import java.util.Set;

public interface PrintService {
  String generatePDF(Print print) throws AxelorException;

  void attachMetaFiles(Print print, Set<MetaFile> metaFiles);
}
