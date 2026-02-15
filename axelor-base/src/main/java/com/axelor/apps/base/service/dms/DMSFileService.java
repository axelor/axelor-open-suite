package com.axelor.apps.base.service.dms;

import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;

public interface DMSFileService {

  /**
   * Finds or create the DMSHome directory for a model
   *
   * @param model
   * @return
   */
  DMSFile findOrCreateHome(Model model);
}
