package com.axelor.apps.base.service.print;

import com.axelor.apps.base.db.Print;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public interface PrintPdfGenerationService {
  File generateFile(Print print, String html, ByteArrayOutputStream pdfOutputStream)
      throws IOException;
}
