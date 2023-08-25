package com.axelor.apps.base.service;

import com.axelor.apps.base.interfaces.PdfViewer;
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
  public DMSFile setDmsFile(MetaFile metaFile, PdfViewer pdfViewer) {
    if (metaFile == null) {
      pdfViewer.setDmsFile(null);

      DMSFile previousDmsFile = pdfViewer.getDmsFile();
      if (previousDmsFile != null) {
        dmsFileRepository.remove(previousDmsFile);
      }
      return null;
    }
    DMSFile dmsFile = metaFiles.attach(metaFile, metaFile.getFileName(), (Model) pdfViewer);
    pdfViewer.setDmsFile(dmsFile);
    return dmsFile;
  }

  @Override
  public String getInlineUrl(DMSFile dmsFile) {
    if (dmsFile == null) {
      return "";
    }
    return String.format("ws/dms/inline/%d", dmsFile.getId());
  }
}
