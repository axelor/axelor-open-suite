package com.axelor.apps.base.service.groupExport;

import com.axelor.apps.base.db.GroupExport;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;

public interface GroupExportService {

  public MetaFile exportAdvanceExports(GroupExport groupExport) throws AxelorException, IOException;
}
