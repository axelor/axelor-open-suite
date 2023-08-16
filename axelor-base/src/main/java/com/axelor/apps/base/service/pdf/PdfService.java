package com.axelor.apps.base.service.pdf;

import com.axelor.apps.base.AxelorException;
import com.axelor.meta.db.MetaFile;

public interface PdfService {
  MetaFile convertImageToPdf(MetaFile metaFile) throws AxelorException;
}
