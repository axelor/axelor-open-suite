package com.axelor.apps.account.service;

import com.axelor.apps.account.db.SubrogationRelease;

public interface SubrogationReleaseWorkflowService {
  void goBackToAccounted(SubrogationRelease subrogationRelease);
}
