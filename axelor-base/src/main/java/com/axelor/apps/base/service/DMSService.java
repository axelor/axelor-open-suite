package com.axelor.apps.base.service;

import com.axelor.apps.base.interfaces.PdfViewer;
import com.axelor.dms.db.DMSFile;
import com.axelor.meta.db.MetaFile;

public interface DMSService {
  DMSFile setDmsFile(MetaFile metaFile, PdfViewer pdfViewer);

  String getInlineUrl(DMSFile dmsFile);
}
