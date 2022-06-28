package com.axelor.apps.account.service;

import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;

public class SubrogationReleaseWorkflowServiceImpl implements SubrogationReleaseWorkflowService {
  @Override
  public void goBackToAccounted(SubrogationRelease subrogationRelease) {
    subrogationRelease.setStatusSelect(SubrogationReleaseRepository.STATUS_ACCOUNTED);
  }
}
