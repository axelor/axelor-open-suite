package com.axelor.apps.base.service.pdf;

import com.axelor.apps.base.AxelorException;
import com.axelor.meta.db.MetaFile;

public interface PdfSignatureService {

  MetaFile digitallySignPdf(
      MetaFile metaFile, MetaFile certificate, String certificatePassword, String reason)
      throws AxelorException;
}
