package com.axelor.apps.base.service.pdf;

import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface PdfService {
  MetaFile convertImageToPdf(MetaFile metaFile) throws IOException;

  MetaFile digitallySignPdf(
      MetaFile metaFile,
      MetaFile certificate,
      String certificatePassword,
      MetaFile imageFile,
      String reason,
      String location)
      throws IOException, GeneralSecurityException;
}
