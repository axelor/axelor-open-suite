package com.axelor.apps.base.service.imports;

import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;

public interface ImportConfigurationService {

  public void updateStatusCompleted(ImportConfiguration configuration, ImportHistory importHistory);

  public void updateStatusError(ImportConfiguration configuration);
}
