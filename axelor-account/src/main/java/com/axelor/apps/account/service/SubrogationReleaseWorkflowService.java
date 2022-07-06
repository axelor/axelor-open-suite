package com.axelor.apps.account.service;

import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.exception.AxelorException;

public interface SubrogationReleaseWorkflowService {
  void goBackToAccounted(SubrogationRelease subrogationRelease) throws AxelorException;
}
