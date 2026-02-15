package com.axelor.apps.base.service.dms;

import com.axelor.apps.base.db.repo.dms.CustomDMSFileRepository;
import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class DMSFileServiceImpl implements DMSFileService {

  @Inject CustomDMSFileRepository dmsFileRepository;

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public DMSFile findOrCreateHome(Model model) {
    return dmsFileRepository.createHome(model);
  }
}
