package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface RequiredDocumentExportService {
  Path exportFile(List<Integer> idList) throws AxelorException, IOException;
}
