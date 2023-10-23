package com.axelor.apps.base.service.pdf;

import com.axelor.apps.base.AxelorException;
import com.axelor.meta.db.MetaFile;
import java.util.List;

public interface PdfService {
  MetaFile convertImageToPdf(MetaFile metaFile) throws AxelorException;

  List<MetaFile> convertImageToPdf(List<MetaFile> metaFileList) throws AxelorException;
}
