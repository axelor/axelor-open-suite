package com.axelor.apps.base.interfaces;

import com.axelor.dms.db.DMSFile;

public interface PdfViewer {
  void setDmsFile(DMSFile dmsFile);

  DMSFile getDmsFile();
}
