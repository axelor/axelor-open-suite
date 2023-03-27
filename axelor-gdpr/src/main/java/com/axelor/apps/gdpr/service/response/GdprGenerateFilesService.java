package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.auth.db.AuditableModel;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import java.io.IOException;

public interface GdprGenerateFilesService {
  MetaFile generateAccessResponseFile(
      GDPRRequest gdprRequest,
      Class<?> modelSelectKlass,
      MetaModel metaModel,
      AuditableModel selectedModel)
      throws AxelorException, ClassNotFoundException, IOException;
}
